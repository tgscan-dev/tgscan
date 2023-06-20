package xyz.tgscan.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.ObjectBuilder;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import xyz.tgscan.domain.Offsets;
import xyz.tgscan.dto.*;
import xyz.tgscan.enums.IdxConstant;
import xyz.tgscan.enums.TgRoomTypeParamEnum;
import xyz.tgscan.utils.RoomLinksUtil;
import xyz.tgscan.utils.SearchLogUtil;

@RestController
@Slf4j
@RequestMapping("/api/search")
public class SearchController {
  private final ElasticsearchClient esClient;
  private final int PAGE_SIZE = 15;
  private final String PRE_TAGS = "<span style='color:red'>";
  private final String POST_TAGS = "</span>";
  @Autowired private SearchLogUtil searchLogUtil;
  @Autowired private HttpServletRequest request;
  @Autowired private RoomLinksUtil roomLinksUtil;
  private HttpClient client = HttpClients.createDefault();

  public SearchController(ElasticsearchClient esClient) {
    this.esClient = esClient;
  }

  private static SearchRespDTO buildRespDTO(HitsMetadata<Object> hits, long totalPage) {
    var docs =
        hits.hits().stream()
            .map(
                tgRoomDocHit -> {
                  var index = tgRoomDocHit.index();
                  if (index.equals(IdxConstant.ROOM_IDX)) {
                    var source = (Map) tgRoomDocHit.source();
                    var name =
                        java.lang.String.join(
                            " ",
                            Optional.ofNullable(tgRoomDocHit.highlight().get(IdxConstant.ROOM_NAME))
                                .orElse(
                                    List.of(
                                        Objects.requireNonNull(source).get("name").toString())));
                    var jhiDesc =
                        java.lang.String.join(
                            " ",
                            Optional.ofNullable(tgRoomDocHit.highlight().get(IdxConstant.ROOM_DESC))
                                .orElse(List.of(source.get("jhiDesc").toString())));
                    return RoomDocDTO.fromTgRoomDoc(source, name, jhiDesc);
                  }
                  if (index.equals(IdxConstant.MESSAGE_IDX)) {
                    var source = (Map) tgRoomDocHit.source();
                    var title =
                        java.lang.String.join(
                            " ",
                            Optional.ofNullable(
                                    tgRoomDocHit.highlight().get(IdxConstant.MESSAGE_TITLE))
                                .orElse(
                                    List.of(
                                        Objects.requireNonNull(source).get("title").toString())));
                    var content =
                        java.lang.String.join(
                            " ",
                            Optional.ofNullable(
                                    tgRoomDocHit.highlight().get(IdxConstant.MESSAGE_CONTENT))
                                .orElse(List.of(source.get("content").toString())));
                    return MessageDocDTO.fromTgMessageDoc(source, title, content);
                  }

                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return new SearchRespDTO(totalPage, docs);
  }

  private static Function<Query.Builder, ObjectBuilder<Query>> buildQuery(
      TgRoomTypeParamEnum type, String kw) {
    // Create queries to match by name and description
    // tg room
    Query matchByName =
        MatchQuery.of(m -> m.field(IdxConstant.ROOM_NAME).query(kw).boost(2f))._toQuery();
    Query matchByDesc =
        MatchQuery.of(r -> r.field(IdxConstant.ROOM_DESC).query(kw).boost(1f))._toQuery();
    Query matchPhraseByName =
        MatchPhraseQuery.of(m -> m.field(IdxConstant.ROOM_NAME).query(kw).boost(4f))._toQuery();
    Query matchPhraseByDesc =
        MatchPhraseQuery.of(r -> r.field(IdxConstant.ROOM_DESC).query(kw).boost(1f))._toQuery();

    // tg message
    Query matchByTitle =
        MatchQuery.of(m -> m.field(IdxConstant.MESSAGE_TITLE).query(kw).boost(2f))._toQuery();
    Query matchByDesc0 =
        MatchQuery.of(r -> r.field(IdxConstant.MESSAGE_CONTENT).query(kw).boost(1f))._toQuery();
    Query matchPhraseByTitle =
        MatchPhraseQuery.of(m -> m.field(IdxConstant.MESSAGE_TITLE).query(kw).boost(4f))._toQuery();
    Query matchPhraseByDesc0 =
        MatchPhraseQuery.of(r -> r.field(IdxConstant.MESSAGE_CONTENT).query(kw).boost(1f))
            ._toQuery();
    return q ->
        q.functionScore(
            f ->
                f.query(
                        q0 ->
                            q0.bool(
                                b1 -> {
                                  if (type != TgRoomTypeParamEnum.ALL) {
                                    b1.filter(
                                        f2 ->
                                            f2.term(
                                                t1 ->
                                                    t1.field(IdxConstant.ROOM_TYPE)
                                                        .value(type.name())));
                                  }
                                  return b1.must(
                                      x1 ->
                                          x1.bool(
                                              y1 ->
                                                  y1.should(matchByName)
                                                      .should(matchByDesc)
                                                      .should(matchPhraseByName)
                                                      .should(matchPhraseByDesc)
                                                      .should(matchByTitle)
                                                      .should(matchByDesc0)
                                                      .should(matchPhraseByTitle)
                                                      .should(matchPhraseByDesc0)));
                                }))
                    .functions(
                        f0 ->
                            f0.fieldValueFactor(
                                    f1 ->
                                        f1.field(IdxConstant.ROOM_MEMBER_CNT)
                                            .factor(1.0)
                                            .modifier(FieldValueFactorModifier.Log1p)
                                            .missing(1.0))
                                .weight(15.0))
                    .functions(
                        f0 ->
                            f0.gauss(
                                    g ->
                                        g.placement(
                                                p ->
                                                    p.origin(JsonData.of("now"))
                                                        .offset(JsonData.of("365d"))
                                                        .decay(0.5)
                                                        .scale(JsonData.of("1d")))
                                            .field(IdxConstant.MESSAGE_SEND_TIME))
                                .weight(30.))
                    .scoreMode(FunctionScoreMode.Sum)
                    .boostMode(FunctionBoostMode.Sum));
  }

  private String getClientIp() {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  @SneakyThrows
  @GetMapping("query")
  public SearchRespDTO query(
      @RequestParam("kw") String kw,
      @RequestParam(value = "p", required = false, defaultValue = "1") Integer page,
      @RequestParam(value = "t", required = false, defaultValue = "ALL") TgRoomTypeParamEnum type) {

    searchLogUtil.log(kw, type.name(), page, getClientIp());

    var query = buildQuery(type, kw);
    var highLight = buildHighLight();

    // Calculate the offset for pagination
    var from = (page - 1) * PAGE_SIZE;

    // Create a builder to build the search request
    Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> builder =
        s ->
            s.index(IdxConstant.ROOM_IDX, IdxConstant.MESSAGE_IDX)
                .size(PAGE_SIZE)
                .from(from)
                .query(query)
                .highlight(highLight);

    // Create the search request builder
    SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
    SearchRequest searchRequest = builder.apply(requestBuilder).build();

    // Execute the search request
    SearchResponse<Object> response = esClient.search(searchRequest, Object.class);

    // Get the hits from the response
    var hits = response.hits();
    var total = Objects.requireNonNull(hits.total()).value();
    var totalPage = total == PAGE_SIZE ? total / PAGE_SIZE : total / PAGE_SIZE + 1;
    return buildRespDTO(hits, totalPage);
  }

  private Function<Highlight.Builder, ObjectBuilder<Highlight>> buildHighLight() {
    return h ->
        h.fields(
                IdxConstant.ROOM_NAME,
                f ->
                    f.matchedFields(IdxConstant.ROOM_NAME)
                        .preTags(PRE_TAGS)
                        .postTags(POST_TAGS)
                        .requireFieldMatch(false))
            .fields(
                IdxConstant.ROOM_DESC,
                f ->
                    f.matchedFields(IdxConstant.ROOM_DESC)
                        .preTags(PRE_TAGS)
                        .postTags(POST_TAGS)
                        .requireFieldMatch(false))
            .fields(
                IdxConstant.MESSAGE_TITLE,
                f ->
                    f.matchedFields(IdxConstant.MESSAGE_TITLE)
                        .preTags(PRE_TAGS)
                        .requireFieldMatch(false)
                        .postTags(POST_TAGS))
            .fields(
                IdxConstant.MESSAGE_CONTENT,
                f ->
                    f.matchedFields(IdxConstant.MESSAGE_CONTENT)
                        .preTags(PRE_TAGS)
                        .requireFieldMatch(false)
                        .fragmentSize(3000)
                        .postTags(POST_TAGS));
  }

  @SneakyThrows
  @GetMapping("autocomplete")
  public List<String> autocomplete(@RequestParam("kw") String kw) {
    var resp = client.execute(new HttpGet("http://api.bing.com/qsonhs.aspx?type=cb&q=" + kw));
    var entity = resp.getEntity();
    var string = EntityUtils.toString(entity);
    var json = JSON.parseObject(string, AutoCompleteDTO.class);
    if (Objects.isNull(json.aS.results)) {
      return Collections.emptyList();
    }
    return json.aS.results.stream()
        .map(x -> x.suggests)
        .flatMap(Collection::stream)
        .map(x -> x.txt)
        .toList();
  }

  @GetMapping(value = "/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
  public byte[] getImage(@PathVariable String imageName) throws IOException {
    File file = new File("icon/" + imageName + ".jpg");
    if (!file.exists()) {
      file = new File("icon/tg.jpg");
    }
    FileInputStream inputStream = new FileInputStream(file);
    byte[] bytes = new byte[(int) file.length()];
    inputStream.read(bytes);
    inputStream.close();
    return bytes;
  }

  @GetMapping("roomLinks")
  public List<Offsets> roomLinks() {
    return roomLinksUtil.roomLinks();
  }
}
