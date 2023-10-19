package xyz.tgscan.schd;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import jakarta.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.domain.RoomDoc;
import xyz.tgscan.repository.RoomDocRepository;
import xyz.tgscan.repository.RoomRepository;

@Component
@Slf4j
public class RoomCrawlJob {
  //  private static final ArrayBlockingQueue<String> proxyQ = new ArrayBlockingQueue<>(50);
  private static final String PROXY_URL =
      "https://tq.lunaproxy.com/getflowip?neek=1029717&num=50&type=2&sep=1&regions=all&ip_si=1&level=1&sb=";
  private final Retryer<String> retryer =
      RetryerBuilder.<String>newBuilder()
          .retryIfExceptionOfType(IOException.class)
          .withStopStrategy(StopStrategies.stopAfterAttempt(5))
          .build();
  private final CloseableHttpClient client = HttpClients.custom().setMaxConnTotal(50).build();
  private final ThreadLocal<String> proxyWrap = new ThreadLocal<>();
  private final ThreadPoolExecutor pool =
      new ThreadPoolExecutor(
          150,
          150,
          0L,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(1024),
          new ThreadFactoryBuilder().setNameFormat("crawler-%d").build(),
          new ThreadPoolExecutor.CallerRunsPolicy());

  @Value("${crawler.room.enable}")
  private boolean enable;

  private volatile boolean rescan = false;
  @Autowired private RoomRepository roomRepository;
  @Autowired private RoomDocRepository roomDocRepository;
  @Autowired private Environment environment;

  public static void main(String[] args) throws IOException {
    var roomCrawlJob = new RoomCrawlJob();
    var url = "https://t.me/sgnoogle_fan";
    var room = new Room().setLink(url);
    var room1 = room;
    roomCrawlJob.fetch(room1);
    System.out.println(room1);
  }

  public void enable() {
    enable = true;
  }

  public void disable() {
    enable = false;
  }

  private void downloadImg(String imageUrl, String localPath) throws IOException {
    if (Files.exists(Path.of(localPath))) {
      return;
    }
    log.debug("download img, url:{}, path:{}", imageUrl, localPath);
    HttpGet httpGet = new HttpGet(imageUrl);
    HttpResponse httpResponse = client.execute(httpGet);
    HttpEntity httpEntity = httpResponse.getEntity();

    if (httpEntity != null) {
      InputStream inputStream = httpEntity.getContent();
      FileOutputStream outputStream = new FileOutputStream(localPath);
      byte[] buffer = new byte[4096];
      int bytesRead = -1;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }
      outputStream.close();
      inputStream.close();
      EntityUtils.consume(httpEntity);
    }
  }

  @PostConstruct
  public void init() {
    //    new Thread(
    //            () -> {
    //              while (true) {
    //                if (!enable && !rescan) {
    //                  try {
    //                    TimeUnit.SECONDS.sleep(5);
    //                  } catch (InterruptedException e) {
    //                    throw new RuntimeException(e);
    //                  }
    //                  continue;
    //                }
    //                List<String> proxy = getProxy();
    //                for (String s : proxy) {
    //                  if (StringUtils.isEmpty(s)) {
    //                    log.error("proxy is empty:{}", s);
    //                  } else {
    //                    proxyQ.offer(s);
    //                  }
    //                }
    //              }
    //            })
    //        .start();
  }

  private boolean isLocal() {
    return Arrays.asList(environment.getActiveProfiles()).contains("dev");
  }

  private List<String> getProxy() {

    while (true) {
      try {
        CloseableHttpResponse resp = HttpClients.createDefault().execute(new HttpGet(PROXY_URL));
        HttpEntity entity = resp.getEntity();
        String s = EntityUtils.toString(entity);
        ObjectMapper objectMapper = new ObjectMapper();
        Map map = objectMapper.readValue(s, Map.class);
        Object data = map.get("data");
        List<String> collect =
            (List<String>)
                ((ArrayList) data)
                    .stream()
                        .map(
                            x ->
                                ((Map) x).get("ip").toString()
                                    + ":"
                                    + ((Map) x).get("port").toString())
                        .collect(Collectors.toList());
        return collect;
      } catch (Exception e) {

        log.debug("no more proxy:" + e.getMessage());
        try {
          TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      }
    }
  }

  private void fetch(Room room) {
    String s = download(room);
    try {
      parseAndSave(room, s);
    } catch (Exception e) {
      log.debug("parse and save err, room:{}, err:{}", JSON.toJSONString(room), e.getMessage());
      throw e;
    }
  }

  private void parseAndSave(Room room, String s) {
    Document doc = Jsoup.parse(s);
    Optional<String> src =
        doc
            .select(
                "body > div.tgme_page_wrap > div.tgme_body_wrap > div > div.tgme_page_photo > a > img")
            .stream()
            .findFirst()
            .map(x -> x.attr("src"));

    String name =
        doc.select(
                "body > div.tgme_page_wrap > div.tgme_body_wrap > div > div.tgme_page_title > span")
            .get(0)
            .text();
    String desc = null;
    try {
      desc =
          doc.select(
                  "body > div.tgme_page_wrap > div.tgme_body_wrap > div > div.tgme_page_description")
              .get(0)
              .text();
    } catch (Exception e) {
      log.error("desc parse err");
    }

    String statiscs =
        doc.select("body > div.tgme_page_wrap > div.tgme_body_wrap > div > div.tgme_page_extra")
            .get(0)
            .text();
    room.setName(name);
    room.setJhiDesc(desc);
    room.setStatus("COLLECTED");
    room.setCollectedAt(Timestamp.valueOf(LocalDateTime.now()));
    src.ifPresent(
        icon -> {
          try {
            retryer.call(
                () -> {
                  var split = room.getLink().split("/");
                  try {
                    downloadImg(icon, "icon/" + split[split.length - 1] + ".jpg");

                  } catch (IOException e) {

                    log.debug("download img fail:{}", JSON.toJSONString(room), e);
                    throw e;
                  }
                  return null;
                });
          } catch (ExecutionException | RetryException e) {
            log.debug("download img fail:{}", JSON.toJSONString(room), e);
          }
        });
    boolean isChannel = statiscs.contains("subscriber");
    if (isChannel) {
      String subscribers = statiscs.replaceAll("subscribers", "").replaceAll("subscriber", "").replaceAll(" ", "");
      room.setType("CHANNEL");
      if (subscribers.equals("no")) {
        subscribers = "0";
      }
      try {
        room.setMemberCnt(Integer.valueOf(subscribers));
        var save = roomRepository.save(room);
        roomDocRepository.save(RoomDoc.fromEntity(save));
      } catch (NumberFormatException e) {
        log.info("parse subscriber cnt err, room:{}, err:{}", JSON.toJSONString(room), e.getMessage());
      }
    }
    boolean isGroup = statiscs.contains("member");
    if (isGroup) {
      String cnt = statiscs.split("member")[0].replaceAll(" ", "");
      if (cnt.equals("no")) {
        cnt = "0";
      }
      try {
        room.setMemberCnt(Integer.valueOf(cnt));
        room.setType("GROUP");
        var save = roomRepository.save(room);
        roomDocRepository.save(RoomDoc.fromEntity(save));
      } catch (NumberFormatException e) {
        log.info("parse member cnt err, room:{}, err:{}", JSON.toJSONString(room), e.getMessage());
      }
    }
    boolean maybeBot =
        doc.select(
                "body > div.tgme_page_wrap > div.tgme_body_wrap > div > div.tgme_page_action > a")
            .text()
            .contains("Send Message");
    if (maybeBot) {
      if (room.getLink().toLowerCase().endsWith("bot")) {
        room.setType("BOT");
      } else{
        room.setType("USER");
      } 
      
      var save = roomRepository.save(room);
      roomDocRepository.save(RoomDoc.fromEntity(save));
    }
  }

  private String download(Room room) {
    try {
      return retryer.call(
          () -> {
            try {
              HttpGet request = new HttpGet(room.getLink());
              //              String proxy = proxyWrap.get();
              //              if (StringUtils.isEmpty(proxy)) {
              //                var take = proxyQ.take();
              //                proxyWrap.set(take);
              //                proxy = take;
              //              }
              //              log.debug("use proxy:{}", proxy);

              //              String[] split = proxy.split(":");
              request.setConfig(
                  RequestConfig.custom()
                      //                      .setProxy(new HttpHost(split[0],
                      // Integer.parseInt(split[1])))
                      .build());

              CloseableHttpResponse resp = client.execute(request);
              HttpEntity entity = resp.getEntity();
              return EntityUtils.toString(entity);
            } catch (IOException e) {
              log.debug(
                  "download err, will retry! room:{}, err:{}",
                  JSON.toJSONString(room),
                  e.getMessage());
              //              proxyWrap.set(proxyQ.take());
              throw e;
            }
          });
    } catch (ExecutionException | RetryException e) {
      log.debug("download err, room:{}，err:{}", JSON.toJSONString(room), e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public void rescan() {
    rescan = true;
    var count = roomRepository.count();
    try {
      int page = 0, size = 50;
      int totalPage = (int) (count / 50);
      while (true) {
        Page<Room> rooms =
            roomRepository.findAll(PageRequest.of(page, size, Sort.Direction.ASC, "id"));

        submitCrawlTasks(rooms);

        page++;
        if (page > totalPage) {
          break;
        }
      }
    } finally {
      rescan = false;
    }
    log.info("done!!!");
  }

  private void submitCrawlTasks(Page<Room> rooms) {
    for (Room room : rooms) {
      pool.submit(
          () -> {
            try {

              log.debug("start fetch:{}", room.getLink());
              fetch(room);
              log.info("end fetch:{}", room.getLink());
            } catch (Throwable e) {
              log.error("fetch err, room:{}, err:{}", JSON.toJSONString(room), e.getMessage());
              if (StringUtils.isNotEmpty(room.getName()) && room.getMemberCnt() > 0) {
                log.warn("need check: " + JSON.toJSONString(room));
                return;
              }
              room.setStatus("ERROR");
              roomRepository.save(room);
            }
          });
    }
  }

  @Scheduled(fixedDelay = 10000)
  public void run() {
    if (!enable) {
      return;
    }
    if (rescan) {
      return;
    }

    int page = 0, size = 50;
    while (true) {
      if (!enable) {
        return;
      }
      Page<Room> rooms = roomRepository.findByStatusOrderByIdAsc("NEW", PageRequest.of(page, size));

      submitCrawlTasks(rooms);
      if (!rooms.hasNext()) {
        break;
      }
      page++;
    }

    log.info("done!!!");
  }
}
