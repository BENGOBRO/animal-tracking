package ru.bengo.animaltracking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Date;

@Data
public class AnimalVisitedLocationDto {
        @NotNull
        @Positive
        private Long id;

        private Date dateTimeOfVisitLocationPoint;

        @NotNull
        @Positive
        private Long locationPointId;
}
