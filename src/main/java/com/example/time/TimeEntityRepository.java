package com.example.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntityRepository extends JpaRepository<TimeEntity, Long> {
    Optional<TimeEntity> findByInstantBetween(Instant from, Instant to);
    Optional<TimeEntity> findByLocalDateTimeBetween(LocalDateTime from, LocalDateTime to);
    Optional<TimeEntity> findByInstantTimestampBetween(Instant from, Instant to);
    Optional<TimeEntity> findByLocalDateTimeTimestampBetween(LocalDateTime from, LocalDateTime to);
}
