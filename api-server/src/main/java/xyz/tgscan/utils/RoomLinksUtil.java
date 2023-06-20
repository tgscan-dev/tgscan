package xyz.tgscan.utils;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Offsets;
import xyz.tgscan.repository.OffsetsRepository;

@Slf4j
@Component
public class RoomLinksUtil {
  private List<Offsets> chatLinks;
  @Autowired private OffsetsRepository offsetsRepository;

  @PostConstruct
  void startup() {
    log.info("start to load chat links");
    chatLinks = offsetsRepository.findAll();
    new Thread(
            () -> {
              while (true) {
                try {
                  chatLinks = offsetsRepository.findAll();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
                try {
                  TimeUnit.SECONDS.sleep(30);
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
              }
            },
            "chat_links_updater")
        .start();
  }

  public List<Offsets> roomLinks() {
    return chatLinks;
  }
}
