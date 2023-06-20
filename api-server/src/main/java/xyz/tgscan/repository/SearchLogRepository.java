package xyz.tgscan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.tgscan.domain.SearchLog;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {
}