package xyz.tgscan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.tgscan.domain.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByIdGreaterThanEqualOrderByIdAsc(Long lastId, Pageable pageable);
}
