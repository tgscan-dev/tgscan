package xyz.tgscan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.tgscan.schd.*;

// Controller class for the crawler
@RestController
@RequestMapping("/internal/crawler")
@Slf4j
public class CrawlerController {

  @Autowired private RoomCrawlJob roomCrawlJob;
  @PostMapping("rescanRoom")
  public void rescanRoom() {
    roomCrawlJob.rescan();
  }
  @PostMapping("enableRoomCrawler")
  public void enableRoomCrawler() {
    roomCrawlJob.enable();
  }

  @PostMapping("disableRoomCrawler")
  public void disableRoomCrawler() {
    roomCrawlJob.disable();
  }
}
