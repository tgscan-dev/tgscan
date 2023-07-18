package xyz.tgscan.schd;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.repository.RoomRepository;

@Component
@Slf4j
public class RoomDataPatchJob {
  private static boolean enable = false;
  @Autowired private RoomRepository roomRepository;

  public void enable() {
    enable = true;
  }

  public void disable() {
    enable = false;
  }

  @Scheduled(fixedDelay = 10000)
  public void run() throws Exception {
    if (!enable) {
      return;
    }

    int page = 0;
    while (true) {
      var all = roomRepository.findAll(PageRequest.of(page, 10000));

      var duplicate =
          all.stream().filter(x -> !x.getLink().equals(x.getLink().toLowerCase())).toList();

      var dupLinks =
          duplicate.stream().map(x -> x.getLink().toLowerCase()).collect(Collectors.toList());

      var has =
          roomRepository.findByLinkIn(dupLinks).stream()
              .map(Room::getLink)
              .collect(Collectors.toSet());

      var toLower =
          duplicate.stream()
              .filter(x -> !has.contains(x.getLink().toLowerCase()))
              .peek(x -> x.setLink(x.getLink().toLowerCase()))
              .toList();
      roomRepository.saveAll(toLower);

      var needRemove =
          duplicate.stream().filter(x -> has.contains(x.getLink().toLowerCase())).toList();

      roomRepository.deleteAll(needRemove);

      log.info("page {} done", page);

      if (all.isLast()) {
        break;
      }
      page++;
    }
  }
}
