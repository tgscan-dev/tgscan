package xyz.tgscan.sync;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.domain.RoomDoc;

@Slf4j
@Component
public abstract class RoomSync extends AbstractSync {
  protected String from = RoomSync.class.getSimpleName();
  @Autowired private ElasticsearchClient esClient;


  protected void save(Page<Room> all, String roomIdx) {
    var partition = Lists.partition(all.getContent(), 500);

    save2es(partition, roomIdx);
  }

  private void save2es(List<List<Room>> partition, String roomIdx) {
    partition.parallelStream()
        .forEach(
            y -> {
              try {
                var bulk =
                    esClient.bulk(
                        x ->
                            x.operations(
                                    y.stream()
                                        .map(RoomDoc::fromEntity)
                                        .map(
                                            x1 ->
                                                BulkOperation.of(
                                                    y1 ->
                                                        y1.index(
                                                            z ->
                                                                z.index(roomIdx)
                                                                    .id(x1.getLink())
                                                                    .document(x1))))
                                        .toList())
                                .refresh(Refresh.False));
              } catch (IOException e) {
                log.error("tg room sync error: ", e);
              }
              //      System.out.println(bulk);
            });
  }
}
