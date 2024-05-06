package com.example.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class Main {

    private final TimeEntityRepository timeEntityRepository;

    public static void main(String[] args) {
        System.out.println("Instant.now() = " + Instant.now());
        System.out.println("Instant.now().atZone(ZoneId.of(\"Asia/Seoul\")) = " + Instant.now()
            .atZone(ZoneId.of("Asia/Seoul")));
        System.out.println("Instant.now().atZone(ZoneId.of(\"Asia/Seoul\")).toInstant() = "
            + Instant.now().atZone(ZoneId.of("Asia/Seoul")).toInstant());
        System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
    }

    @GetMapping("/save")
    public void save() {
        Instant instant = Instant.now();
        LocalDateTime localDateTime = LocalDateTime.now();

        System.out.println("instant = " + instant);
        System.out.println("localDateTime = " + localDateTime);
        ZonedDateTime zonedDateTime = Instant.now().atZone(ZoneId.systemDefault());
        System.out.println("zonedDateTime = " + zonedDateTime);

        TimeEntity timeEntity =
            TimeEntity.builder()
                .instant(instant)
                .localDateTime(localDateTime)
                .build();

        timeEntityRepository.save(timeEntity);
    }

    @GetMapping("/get")
    public void get() {
        TimeEntity timeEntity = timeEntityRepository.findById(1L).orElseThrow();
        System.out.println(timeEntity.toString());
    }

    @GetMapping("/get2")
    public void get2() {
        Instant toInstant = Instant.now();
        Instant fromInstant = toInstant.minusSeconds(600);

        LocalDateTime toLocalDateTime = LocalDateTime.now();
        LocalDateTime fromLocalDateTime = toLocalDateTime.minusSeconds(600);

        System.out.println("instant = " + fromInstant + " ~ " + toInstant);
        System.out.println("localDateTime = " + fromLocalDateTime + " ~ " + toLocalDateTime);
        System.out.println("---------------------------------------------------------------------");

        TimeEntity findByInstant = timeEntityRepository.findByInstantBetween(fromInstant, toInstant)
            .orElse(null);
        TimeEntity findByInstantTimestamp = timeEntityRepository.findByInstantTimestampBetween(fromInstant,
            toInstant).orElse(null);
        TimeEntity findByLocalDateTime = timeEntityRepository.findByLocalDateTimeBetween(fromLocalDateTime,
            toLocalDateTime).orElse(null);
        TimeEntity findByLocalDateTimeTimestamp = timeEntityRepository.findByLocalDateTimeTimestampBetween(
            fromLocalDateTime, toLocalDateTime).orElse(null);

        System.out.println("findByInstant = " + findByInstant);
        System.out.println("findByInstantTimestamp = " + findByInstantTimestamp);
        System.out.println("findByLocalDateTime = " + findByLocalDateTime);
        System.out.println("findByLocalDateTimeTimestamp = " + findByLocalDateTimeTimestamp);
        System.out.println("---------------------------------------------------------------------");

        Instant before20240430 = Instant.ofEpochSecond(1714432569);
        Instant after20240430 = Instant.ofEpochSecond(1714436169);

        System.out.println("before20240430 = " + before20240430); // UTC 2024-04-29 23:16:09
        System.out.println("after20240430 = " + after20240430); // UTC 2024-04-30 00:16:09
        System.out.println("---------------------------------------------------------------------");
        TimeEntity beforeEntity = timeEntityRepository.save(
            TimeEntity.builder().instant(before20240430).build());
        TimeEntity afterEntity = timeEntityRepository.save(
            TimeEntity.builder().instant(after20240430).build());
        System.out.println("beforeEntity = " + beforeEntity);
        System.out.println("afterEntity = " + afterEntity);
        System.out.println("---------------------------------------------------------------------");

        LocalDateTime startOfDay = LocalDate.of(2024, 4, 29).atStartOfDay();
        Instant startOfDayInstant = startOfDay.toInstant(ZoneOffset.UTC);
        Instant endOfDayInstant = startOfDay.plusHours(23).plusMinutes(59).plusSeconds(59).toInstant(ZoneOffset.UTC);
        System.out.println("LocalDate to Instant = " + startOfDayInstant + " ~ " + endOfDayInstant);
        System.out.println("---------------------------------------------------------------------");

        TimeEntity localDateToInstant = timeEntityRepository.findByInstantBetween(startOfDayInstant,
            endOfDayInstant).orElse(null);
        System.out.println("localDateToInstant = " + localDateToInstant);
    }



}
