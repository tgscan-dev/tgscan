package xyz.tgscan.repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import xyz.tgscan.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

  Page<Room> findByStatusOrderByIdAsc(String status, Pageable pageable);

  Page<Room> findByIdGreaterThanEqualOrderByIdAsc(Long id, Pageable pageable);

  List<Room> findByLinkIn(Collection<String> links);
}
