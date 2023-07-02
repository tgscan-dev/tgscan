package xyz.tgscan.controller;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
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
import xyz.tgscan.enums.TgRoomTypeParamEnum;
import xyz.tgscan.service.SearchService;
import xyz.tgscan.utils.RoomLinksUtil;
import xyz.tgscan.utils.SearchLogUtil;

@RestController
@Slf4j
@RequestMapping("/api/search")
public class SearchController {

  @Autowired private SearchLogUtil searchLogUtil;
  @Autowired private HttpServletRequest request;
  @Autowired private RoomLinksUtil roomLinksUtil;
  private HttpClient client = HttpClients.createDefault();
  @Autowired private SearchService searchService;

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

  @GetMapping("query")
  public SearchRespDTO query(
      @RequestParam("kw") String kw,
      @RequestParam(value = "p", required = false, defaultValue = "1") Integer page,
      @RequestParam(value = "t", required = false, defaultValue = "ALL") TgRoomTypeParamEnum type) {

    searchLogUtil.log(kw, type.name(), page, getClientIp());
    return searchService.recall(kw, page, type);
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
