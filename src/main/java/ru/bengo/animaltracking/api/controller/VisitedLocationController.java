package ru.bengo.animaltracking.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.bengo.animaltracking.api.dto.VisitedLocationDto;
import ru.bengo.animaltracking.api.dto.VisitedLocationUpdateDto;
import ru.bengo.animaltracking.api.dto.mapper.VisitedLocationMapper;
import ru.bengo.animaltracking.service.VisitedLocationService;
import ru.bengo.animaltracking.store.entity.VisitedLocation;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/animals/{animalId}/locations")
@RequiredArgsConstructor
@Slf4j
public class VisitedLocationController {

    private final VisitedLocationService visitedLocationService;
    private final VisitedLocationMapper visitedLocationMapper;

    @PostMapping("/{pointId}")
    public ResponseEntity<VisitedLocationDto> createVisitedLocation(@PathVariable Long animalId,
                                                                    @PathVariable Long pointId) {
        VisitedLocation visitedLocation = visitedLocationService.create(animalId, pointId);
        return ResponseEntity.status(HttpStatus.CREATED).body(visitedLocationMapper.toDto(visitedLocation));
    }

    @GetMapping
    public ResponseEntity<List<VisitedLocationDto>> searchVisitedLocations(
            @PathVariable Long animalId,
            @RequestParam(required = false) Date startDateTime,
            @RequestParam(required = false) Date endDateTime,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        List<VisitedLocation> visitedLocations = visitedLocationService.search(animalId, startDateTime,
                endDateTime, from, size);
        return ResponseEntity.ok(visitedLocations.stream().map(visitedLocationMapper::toDto).toList());
    }

    @PutMapping
    public ResponseEntity<VisitedLocationDto> updateVisitedLocation(
            @PathVariable Long animalId,
            @RequestBody VisitedLocationUpdateDto visitedLocationUpdateDto){
        VisitedLocation visitedLocation = visitedLocationService.update(animalId, visitedLocationUpdateDto);
        return ResponseEntity.ok(visitedLocationMapper.toDto(visitedLocation));
    }

    @DeleteMapping("/{visitedPointId}")
    public ResponseEntity<Void> deleteVisitedLocation(@PathVariable Long animalId, @PathVariable Long visitedPointId) {
        visitedLocationService.delete(animalId, visitedPointId);
        return ResponseEntity.ok().build();
    }



}
