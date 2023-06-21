package xyz.tgscan.schd;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.repository.RoomRepository;

@Component
@Slf4j
public class RoomDataPatchJob implements CommandLineRunner {
  private static final boolean enable = false;
  @Autowired private RoomRepository roomRepository;

  @Override
  public void run(String... args) throws Exception {
    if (!enable) {
      return;
    }

    new Thread(
            () -> {
              var all = roomRepository.findAll();
              var collect =
                  all.stream().collect(Collectors.groupingBy(room -> room.getLink().toLowerCase()));
              var duplicate =
                  collect.entrySet().stream().filter(x -> x.getValue().size() > 1).toList();
              for (Map.Entry<String, List<Room>> entry : duplicate) {
                entry.getValue().stream()
                    .skip(1)
                    .forEach(
                        entity -> {
                          roomRepository.delete(entity);
                          log.info("delete duplicate room {}", entity.getId());
                        });
              }
              log.info("done");
            })
        .start();
  }
}
