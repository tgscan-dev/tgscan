package xyz.tgscan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.tgscan.schd.*;

// Controller class for the crawler
@RestController
@RequestMapping("/internal/data-patch")
@Slf4j
public class DataPatchController {

  @Autowired private RoomDataPatchJob dataPatchJob;

  @PostMapping("enable")
  public void enableRoomCrawler() {
    dataPatchJob.enable();
  }

  @PostMapping("disable")
  public void disableRoomCrawler() {
    dataPatchJob.disable();
  }
}
