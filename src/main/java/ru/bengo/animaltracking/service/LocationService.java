package ru.bengo.animaltracking.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.bengo.animaltracking.model.Location;

import java.util.Optional;

public interface LocationService {

    Optional<Location> getLocation(@NotNull @Positive Long id);
    Location addLocation(@Valid Location location);
    void deleteLocation(@NotNull @Positive Long id);
    Location changeLocation(@Valid Location location, @NotNull @Positive Long id);
}
