package xyz.tgscan.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.tgscan.enums.IdxConstant;
import xyz.tgscan.sync.*;

@RestController
@RequestMapping("/internal/sync")
public class SyncController {
  @Autowired private RoomFullSync roomFullSync;
  @Autowired private MessageFullSync messageFullSync;
  @Autowired private MessageIncSync messageIncSync;

  @PostMapping("fullSync4room")
  public void fullSync4room(@RequestParam(value = "idx",required = false) String idx) {
    if (StringUtils.isEmpty(idx)) {
      idx = IdxConstant.ROOM_IDX;
    }
    roomFullSync.run(idx);
  }

  @PostMapping("fullSync4message")
  public void fullSync4message(@RequestParam(value = "idx",required = false) String idx) {
    if (StringUtils.isEmpty(idx)) {
      idx = IdxConstant.MESSAGE_IDX;
    }
    messageFullSync.run(idx);
  }



  @PostMapping("enableIncSync4message")
  public void enableIncSync4message() {
    messageIncSync.enable();
  }

  @PostMapping("disableIncSync4message")
  public void disableIncSync4message() {
    messageIncSync.disable();
  }

}
