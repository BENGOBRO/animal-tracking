package ru.bengo.animaltracking.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
public class AnimalVisitedLocation {

    @Id
    @Column(nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime dateTimeOfVisitLocationPoint;

    private Long locationPointId;


}