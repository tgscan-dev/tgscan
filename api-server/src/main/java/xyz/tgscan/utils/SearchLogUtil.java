package xyz.tgscan.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.domain.SearchLog;
import xyz.tgscan.enums.TgRoomStatusEnum;
import xyz.tgscan.repository.RoomRepository;
import xyz.tgscan.repository.SearchLogRepository;

@Slf4j
@Component
public class SearchLogUtil implements CommandLineRunner {

  private final ArrayBlockingQueue<SearchLog> q = new ArrayBlockingQueue<>(2048);
  @Autowired private SearchLogRepository searchLogRepository;
  @Autowired private RoomRepository roomRepository;

  public static boolean isValidGroupName(String input) {
    String regex = "^@[a-zA-Z][a-zA-Z0-9_]{3,31}$";
    return Pattern.matches(regex, input);
  }

  public void log(String kw, String t, Integer p, String ip) {
    var searchAt = new Timestamp(System.currentTimeMillis());
    var log = new SearchLog().setKw(kw).setT(t).setP(p).setIp(ip).setSearchAt(searchAt);
    q.offer(log);
  }

  @Override
  public void run(String... args) throws Exception {
    new Thread(
            () -> {
              while (true) {
                try {
                  var size = q.size();
                  var els = new ArrayList<SearchLog>();
                  for (int i = 0; i < size; i++) {
                    var log = q.take();
                    els.add(log);
                  }
                  searchLogRepository.saveAll(els);
                  var links =
                      els.stream()
                          .map(SearchLog::getKw)
                          .filter(SearchLogUtil::isValidGroupName)
                          .map(String::toLowerCase)
                          .distinct()
                          .map(x -> "https://t.me/" + x.substring(1))
                          .toList();
                  @SuppressWarnings("DuplicatedCode")
                  var has =
                      roomRepository.findByLinkIn(links).stream()
                          .map(Room::getLink)
                          .collect(Collectors.toSet());
                  var notHas = links.stream().filter(x -> !has.contains(x)).toList();
                  roomRepository.saveAll(
                      notHas.stream()
                          .map(x -> new Room().setLink(x).setStatus(TgRoomStatusEnum.NEW.name()))
                          .toList());
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
                try {
                  TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                  throw new RuntimeException(e);
                }
              }
            },
            SearchLogUtil.class.getSimpleName())
        .start();
  }
}
