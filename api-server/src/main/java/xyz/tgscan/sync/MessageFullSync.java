package xyz.tgscan.sync;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import xyz.tgscan.repository.MessageRepository;

@Slf4j
@Component
public class MessageFullSync extends MessageSync {
  @Autowired private MessageRepository messageRepository;

  @SneakyThrows
  public void run(String messageIdx) {
    int page = 0;
    Long lastId = 0L;
    while (true) {
      PageRequest pageRequest = PageRequest.of(page, 500, Sort.Direction.ASC, "id");
      var all = messageRepository.findAll(pageRequest);
      if (all.isEmpty()) {
        return;
      }
      save(all, messageIdx);
      log.info("tg message sync page: {} ", page);
      if (!all.hasNext()) {
        break;
      }
      page++;
      lastId = all.toList().get(all.toList().size() - 1).getId();
    }
    updateLastId(from, lastId);
    log.info("tg message sync done!");
  }
}
