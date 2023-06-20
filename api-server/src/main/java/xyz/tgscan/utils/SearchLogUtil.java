package xyz.tgscan.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.SearchLog;
import xyz.tgscan.repository.SearchLogRepository;

@Slf4j
@Component
public class SearchLogUtil implements CommandLineRunner {

  @Autowired private SearchLogRepository searchLogRepository;
  private final ArrayBlockingQueue<SearchLog> q = new ArrayBlockingQueue<>(2048);

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
