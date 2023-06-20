package xyz.tgscan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.tgscan.domain.Offsets;

public interface OffsetsRepository extends JpaRepository<Offsets, Long> {}
