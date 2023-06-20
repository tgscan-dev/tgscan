package xyz.tgscan.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xyz.tgscan.enums.IdxConstant;
import xyz.tgscan.repository.MessageRepository;

@Slf4j
@Component
public class MessageIncSync extends MessageSync {
  @Autowired private MessageRepository messageRepository;

  @Value("${sync.inc.message.enable}")
  private boolean enable;

  public void enable() {
    enable = true;
  }

  public void disable() {
    enable = false;
  }

  @Scheduled(fixedDelay = 10000)
  public void sync() {
    if (!enable) {
      return;
    }
    var lastId = getLastId(from);
    log.info("tg message inc start sync, last id: {} ", lastId);

    var msgs =
        messageRepository.findByIdGreaterThanEqualOrderByIdAsc(lastId, Pageable.ofSize(1000));

    if (msgs.isEmpty()) {
      return;
    }

    save(msgs, IdxConstant.MESSAGE_IDX);

    var list = msgs.toList();
    var newLastId = list.get(list.size() - 1).getId();
    updateLastId(from, newLastId);

    log.info("tg message inc sync success, new last id: {} ", newLastId);
  }
}
