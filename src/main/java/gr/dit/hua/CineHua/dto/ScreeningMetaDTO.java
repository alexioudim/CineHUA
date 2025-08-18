package gr.dit.hua.CineHua.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScreeningMetaDTO(
        Long id, String movieTitle, String auditoriumName, LocalDate date, LocalTime startTime
) {}
