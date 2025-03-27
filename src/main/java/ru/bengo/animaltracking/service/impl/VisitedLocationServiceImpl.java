package ru.bengo.animaltracking.service.impl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bengo.animaltracking.api.dto.VisitedLocationUpdateDto;
import ru.bengo.animaltracking.store.entity.Animal;
import ru.bengo.animaltracking.store.entity.Location;
import ru.bengo.animaltracking.store.entity.VisitedLocation;
import ru.bengo.animaltracking.api.exception.BadRequestException;
import ru.bengo.animaltracking.api.exception.NotFoundException;
import ru.bengo.animaltracking.api.model.LifeStatus;
import ru.bengo.animaltracking.api.model.Message;
import ru.bengo.animaltracking.store.repository.VisitedLocationRepository;
import ru.bengo.animaltracking.service.AnimalService;
import ru.bengo.animaltracking.service.LocationService;
import ru.bengo.animaltracking.service.VisitedLocationService;

import java.util.Date;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class VisitedLocationServiceImpl implements VisitedLocationService {

    private final VisitedLocationRepository visitedLocationRepository;
    private final LocationService locationService;
    private final AnimalService animalService;

    @Override
    public VisitedLocation create(@NotNull @Positive Long animalId, @NotNull @Positive Long locationId) {
        var location = locationService.get(locationId);
        var animal = animalService.get(animalId);
        var visitedLocations = animal.getVisitedLocations();

        if (animal.getLifeStatus().equals(LifeStatus.DEAD)) {
            throw new BadRequestException(Message.ANIMAL_IS_DEAD.getInfo());
        }
        if (animal.getChippingLocation().getId().equals(locationId) && visitedLocations.isEmpty()) {
            throw new BadRequestException(Message.ANIMAL_IN_CHIPPING_LOCATION.getInfo());
        }
        if (animal.getChippingLocation().equals(location)) {
            throw new BadRequestException(Message.CHIPPING_LOCATION_IS_VISITED_LOCATION.getInfo());
        }

        int lastIndex = visitedLocations.size() - 1;
        if (!visitedLocations.isEmpty() && visitedLocations.get(lastIndex).getLocation().equals(location)) {
            throw new BadRequestException(Message.ANIMAL_IS_ALREADY_LOCATED.getInfo());
        }

        return visitedLocationRepository.save(convertToEntity(location, animal));
    }

    @Override
    public List<VisitedLocation> search(@NotNull @Positive Long animalId, Date startDateTime,
                                        Date endDateTime, @Min(0) Integer from, @Min(1) Integer size) {
        List<VisitedLocation> visitedLocations = visitedLocationRepository.search(startDateTime, endDateTime);

        return visitedLocations.stream().skip(from).limit(size).toList();
    }

    @Override
    public VisitedLocation update(@NotNull @Positive Long animalId,
                                  @Valid VisitedLocationUpdateDto visitedLocationUpdateDto) {
        var animal = animalService.get(animalId);
        var visitedLocation = visitedLocationRepository.findById(visitedLocationUpdateDto.getVisitedLocationPointId())
                .orElseThrow(() -> new NotFoundException(Message.ANIMAL_VISITED_LOCATION_NOT_FOUND.getInfo()));
        var newLocation = locationService.get(visitedLocationUpdateDto.getLocationPointId());
        var visitedLocations = animal.getVisitedLocations();

        if (!visitedLocations.contains(visitedLocation)) {
            throw new NotFoundException(Message.ANIMAL_VISITED_LOCATION_NOT_FOUND.getInfo());
        }
        if (newLocation.equals(animal.getChippingLocation())) {
            throw new BadRequestException(Message.CHIPPING_LOCATION_IS_VISITED_LOCATION.getInfo());
        }
        if (visitedLocations.getFirst().equals(visitedLocation) && newLocation.equals(animal.getChippingLocation())) {
            throw new NotFoundException(Message.FIRST_VISITED_POINT_TO_CHIPPING_POINT.getInfo());
        }
        if (visitedLocation.getLocation().equals(newLocation)) {
            throw new BadRequestException(Message.SAME_POINT.getInfo());
        }

        visitedLocation.setLocation(newLocation);
        return visitedLocationRepository.save(visitedLocation);
    }

    @Override
    public void delete(@NotNull @Positive Long animalId, @NotNull @Positive Long visitedLocationId) {

        var animal = animalService.get(animalId);
        var visitedLocation = visitedLocationRepository.findById(visitedLocationId)
                .orElseThrow(() -> new NotFoundException(Message.ANIMAL_VISITED_LOCATION_NOT_FOUND.getInfo()));
        var visitedLocations = animal.getVisitedLocations();

        if (!visitedLocations.contains(visitedLocation)) {
            throw new NotFoundException(Message.ANIMAL_VISITED_LOCATION_NOT_FOUND.getInfo());
        }

        visitedLocationRepository.deleteById(visitedLocationId);
    }

    private VisitedLocation convertToEntity(Location locationPoint, Animal animal) {
        return VisitedLocation.builder()
                .location(locationPoint)
                .animal(animal)
                .build();
    }
}
