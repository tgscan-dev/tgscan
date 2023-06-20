package xyz.tgscan.sync;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import xyz.tgscan.domain.Room;
import xyz.tgscan.enums.TgRoomStatusEnum;
import xyz.tgscan.repository.RoomRepository;

@Slf4j
@Component
public class RoomFullSync extends RoomSync  {
  @Autowired private RoomRepository tgRoomRepository;

  @SneakyThrows
  public void run(String roomIdx) {
    // todo export thread count, batch size to config
    int page = 0;
    Long lastId = 0L;
    while (true) {
      PageRequest pageRequest = PageRequest.of(page, 2500);
      Page<Room> all =
          tgRoomRepository.findByStatusOrderByIdAsc(TgRoomStatusEnum.COLLECTED.name(), pageRequest);
      if (all.isEmpty()) {
        return;
      }
      save(all, roomIdx);
      log.info("tg room sync page: {} ", page);

      if (!all.hasNext()) {
        break;
      }
      page++;
      lastId = all.toList().get(all.toList().size() - 1).getId();
    }
    updateLastId(from, lastId);
    log.info("tg room sync done!");
  }
}
