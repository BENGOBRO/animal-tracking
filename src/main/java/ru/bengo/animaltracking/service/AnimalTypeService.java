package ru.bengo.animaltracking.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.bengo.animaltracking.api.dto.AnimalTypeDto;
import ru.bengo.animaltracking.store.entity.AnimalType;

public interface AnimalTypeService {
    AnimalType create(@Valid AnimalTypeDto animalType);

    AnimalType get(@NotNull @Positive Long id);

    AnimalType update(@NotNull @Positive Long id, @Valid AnimalTypeDto animalType);

    void delete(@NotNull @Positive Long id);
}
