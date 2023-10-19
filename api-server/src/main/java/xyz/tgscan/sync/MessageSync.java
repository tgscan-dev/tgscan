package xyz.tgscan.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.*;
import xyz.tgscan.enums.TgRoomStatusEnum;
import xyz.tgscan.repository.RoomRepository;
import xyz.tgscan.utils.RoomLinksUtil;

@Slf4j
@Component
public abstract class MessageSync extends AbstractSync {

  protected String from = MessageSync.class.getSimpleName();
  @Autowired private ElasticsearchClient esClient;
  @Autowired private RoomRepository roomRepository;
  @Autowired private RoomLinksUtil roomLinksUtil;

  public static List<String> parseRoomLinks(String text) {
    List<String> links = new ArrayList<>();
    String regex = "https?://t\\.me/[a-zA-Z0-9_-]+";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      String url = matcher.group();
      links.add(url);
    }
    return links;
  }

  @SneakyThrows
  protected void save(Page<Message> all, String messageIdx) {
    var docs =
        all
            .filter(x -> StringUtils.isNotEmpty(x.getContent()))
            .map(MessageDoc::fromEntity)
            .filter(x -> StringUtils.isNotEmpty(x.getContent()))
            .stream()
            .toList();
    var roomLinks = roomLinksUtil.roomLinks();
    var chatIds4links =
        roomLinks.stream()
            .filter(Offsets::getCrawlLink)
            .map(Offsets::getUsername)
            .collect(Collectors.toSet());

    saveRoom(docs, chatIds4links);

    var messageDocs = docs.stream().filter(x -> !chatIds4links.contains(x.getUsername())).toList();

    save2es(messageDocs, messageIdx);
  }

  private void save2es(List<MessageDoc> messageDocs, String messageIdx) throws IOException {
    if (messageDocs.isEmpty()) {
      return;
    }
    var bulk =
        esClient.bulk(
            x ->
                x.operations(
                        messageDocs.stream()
                            .map(
                                x1 ->
                                    BulkOperation.of(
                                        y1 ->
                                            y1.index(
                                                z ->
                                                    z.index(messageIdx)
                                                        .id(x1.getUsername() + "#" + x1.getOffset())
                                                        .document(x1))))
                            .toList())
                    .refresh(Refresh.True));
    for (BulkResponseItem item : bulk.items()) {
      if (item.error() != null) {
        log.error(item.error().toString());
        log.error(item.error().reason());
      }
    }
  }

  private void saveRoom(List<MessageDoc> docs, Set<String> chatIds4links) {
    if (docs.isEmpty()) {
      return;
    }
    var linksDocs = docs.stream().filter(x -> chatIds4links.contains(x.getUsername())).toList();
    var links =
        linksDocs.stream()
            .flatMap(x -> parseRoomLinks(x.getContent()).stream())
            .filter(StringUtils::isNotEmpty)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    var has =
        roomRepository.findByLinkIn(links).stream().map(Room::getLink).collect(Collectors.toSet());
    var notHas = links.stream().filter(x -> !has.contains(x)).toList();
    roomRepository.saveAll(
        notHas.stream()
            .map(x -> new Room().setLink(x).setStatus(TgRoomStatusEnum.NEW.name()))
            .toList());
    log.info("save room links: {}", notHas);
  }
}
