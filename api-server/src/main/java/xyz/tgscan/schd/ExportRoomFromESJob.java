package xyz.tgscan.schd;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import co.elastic.clients.util.ObjectBuilder;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.enums.IdxConstant;
import xyz.tgscan.enums.TgRoomStatusEnum;
import xyz.tgscan.repository.RoomRepository;

@Component
public class ExportRoomFromESJob {
  @Autowired private ElasticsearchClient esClient;

  @Autowired private RoomRepository roomRepository;

//  @PostConstruct
  public void init() throws IOException {
    // Create a builder to build the search request
    Function<SearchRequest.Builder, ObjectBuilder<SearchRequest>> builder =
        s ->
            s.index(IdxConstant.ROOM_IDX)
                .size(1000)
                .query(QueryBuilders.matchAll().build()._toQuery())
                .scroll(f -> f.offset(0))
                .source(
                    new SourceConfig.Builder()
                        .filter(
                            new SourceFilter.Builder().includes(IdxConstant.ROOM_USERNAME).build())
                        .build());

    // Create the search request builder
    SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
    SearchRequest searchRequest = builder.apply(requestBuilder).build();

    var searchResponse = esClient.search(searchRequest, Object.class);
    String scrollId = searchResponse.scrollId();
    var searchHits = searchResponse.hits().hits();
    var rooms =
        searchHits.stream()
            .map(hit -> ((LinkedHashMap) hit.source()).get("userName").toString())
            .map(x -> new Room().setLink("https://t.me/" + x).setStatus("NEW"))
            .toList();
    saveRooms(rooms);

    while (searchHits.size() > 0) {

      var build = new ScrollRequest.Builder().scrollId(scrollId).build();
      var scroll = esClient.scroll(build, Object.class);
      scrollId = scroll.scrollId();
      searchHits = scroll.hits().hits();
      var rooms0 =
          searchHits.stream()
              .map(hit -> ((LinkedHashMap) hit.source()).get("userName").toString())
              .map(x -> new Room().setLink("https://t.me/" + x).setStatus("NEW"))
              .toList();
      saveRooms(rooms0);
    }
  }

  private void saveRooms(List<Room> rooms) {
    var links =
        rooms.stream().map(Room::getLink).map(String::toLowerCase).collect(Collectors.toSet());
    var has =
        roomRepository.findByLinkIn(links).stream().map(Room::getLink).collect(Collectors.toSet());
    var notHas = links.stream().filter(x -> !has.contains(x)).toList();
    roomRepository.saveAll(
        notHas.stream()
            .map(x -> new Room().setLink(x).setStatus(TgRoomStatusEnum.NEW.name()))
            .toList());
  }
}
