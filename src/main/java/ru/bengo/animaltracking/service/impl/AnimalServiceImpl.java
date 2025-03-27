package ru.bengo.animaltracking.service.impl;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bengo.animaltracking.api.dto.AnimalDto;
import ru.bengo.animaltracking.api.dto.TypeDto;
import ru.bengo.animaltracking.api.dto.mapper.AnimalMapper;
import ru.bengo.animaltracking.api.exception.BadRequestException;
import ru.bengo.animaltracking.api.exception.ConflictException;
import ru.bengo.animaltracking.api.exception.NotFoundException;
import ru.bengo.animaltracking.api.model.LifeStatus;
import ru.bengo.animaltracking.api.model.Message;
import ru.bengo.animaltracking.service.AccountService;
import ru.bengo.animaltracking.service.AnimalService;
import ru.bengo.animaltracking.service.AnimalTypeService;
import ru.bengo.animaltracking.service.LocationService;
import ru.bengo.animaltracking.store.entity.Animal;
import ru.bengo.animaltracking.store.entity.AnimalType;
import ru.bengo.animaltracking.store.repository.AnimalRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class AnimalServiceImpl implements AnimalService {

    private final AnimalMapper animalMapper;
    private final AnimalRepository animalRepository;
    private final AccountService accountService;
    private final AnimalTypeService animalTypeService;
    private final LocationService locationService;

    @Override
    public Animal create(AnimalDto animalDto) {
        var animalTypesIds = animalDto.getAnimalTypesIds();
        var isAnimalTypesExists = animalTypesIds == null || animalTypesIds.isEmpty();
        if (isAnimalTypesExists) {
            throw new BadRequestException(Message.ANIMAL_TYPE_NOT_FOUND.getInfo());
        }
        if (hasAnimalTypesDuplicates(animalTypesIds)) {
            throw new ConflictException(Message.ANIMAL_TYPES_HAS_DUPLICATES.getInfo());
        }

        var chippingLocation = locationService.get(animalDto.getChippingLocationId());
        var chipper = accountService.get(animalDto.getChipperId());
        var animalTypes = getAnimalTypes(animalTypesIds);

        return animalRepository.save(animalMapper.toEntity(animalDto, chippingLocation, chipper, animalTypes));
    }


    @Override
    public Animal get(@NotNull @Positive Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Message.ANIMAL_NOT_FOUND.getInfo()));
    }


    @Override
    public Animal update(@NotNull @Positive Long animalId, AnimalDto animalDto) {
        var animal = get(animalId);
        var isAnimalDead = isDead(animal.getLifeStatus());
        if (isAnimalDead) {

            var newLifeStatus = animalDto.getLifeStatus();
            var isNewStatusAlive = newLifeStatus.equals(LifeStatus.ALIVE.name());
            if (isNewStatusAlive) {
                throw new BadRequestException(Message.UPDATE_DEAD_TO_ALIVE.getInfo());
            }
        }

        var isAnimalHasVisitedLocations = !animal.getVisitedLocations().isEmpty();
        var newChippingLocation = locationService.get(animalDto.getChippingLocationId());
        if (isAnimalHasVisitedLocations) {

            var firstVisitedLocationId = animal.getVisitedLocations().getFirst().getId();
            var isNewChippingLocationIdEqualsFirstVisitedLocation =
                    firstVisitedLocationId.equals(newChippingLocation.getId());
            if (isNewChippingLocationIdEqualsFirstVisitedLocation) {
                throw new BadRequestException(
                        Message.NEW_CHIPPING_LOCATION_ID_EQUALS_FIRST_VISITED_LOCATION.getInfo());
            }
        }

        var newChipper = accountService.get(animalDto.getChipperId());
        animalMapper.toEntity(animalDto, newChippingLocation, newChipper, animal);
        return animalRepository.save(animal);
    }

    @Override
    public void delete(@NotNull @Positive Long animalId) {
        Animal animal = get(animalId);

        var isAnimalHasVisitedLocations = !animal.getVisitedLocations().isEmpty();
        if (isAnimalHasVisitedLocations) {
            throw new BadRequestException(Message.ANIMAL_ASSOCIATION.getInfo());
        }

        animalRepository.deleteById(animalId);
    }

    @Override
    public List<Animal> search(LocalDateTime startDateTime, LocalDateTime endDateTime,
                               @Positive Integer chipperId, @Positive Long chippingLocationId,
                               String lifeStatus, String gender,
                               @Min(0) Integer from, @Min(1) Integer size) {
        PageRequest pageRequest = PageRequest.ofSize(size + from);
        List<Animal> foundAnimals = animalRepository.search(startDateTime, endDateTime, chipperId,
                chippingLocationId, lifeStatus, gender, pageRequest);
        return foundAnimals
                .stream()
                .skip(from)
                .toList();
    }

    @Override
    public Animal addAnimalTypeToAnimal(@NotNull @Positive Long animalId, @NotNull @Positive Long typeId) {
        Animal animal = get(animalId);
        AnimalType animalType = animalTypeService.get(typeId);
        List<AnimalType> animalTypes = animal.getAnimalTypes();

        var isAnimalContainsNewType = animalTypes.contains(animalType);
        if (isAnimalContainsNewType) {
            throw new ConflictException(Message.ANIMAL_TYPES_CONTAIN_NEW_ANIMAL_TYPE.getInfo());
        }

        animalTypes.add(animalType);
        animal.setAnimalTypes(animalTypes);
        return animalRepository.save(animal);
    }

    @Override
    public Animal updateAnimalTypeInAnimal(@NotNull @Positive Long animalId, TypeDto typeDto) {
        Animal animal = get(animalId);
        List<AnimalType> animalTypes = animal.getAnimalTypes();
        AnimalType oldType = animalTypeService.get(typeDto.getOldTypeId());

        var isAnimalContainsOldType = animalTypes.contains(oldType);
        if (!isAnimalContainsOldType) {
            throw new NotFoundException(Message.ANIMAL_TYPE_NOT_FOUND.getInfo());
        }

        AnimalType newType = animalTypeService.get(typeDto.getNewTypeId());
        var isAnimalContainsNewType = animalTypes.contains(newType)
                || (animalTypes.contains(oldType) && animalTypes.contains(newType));
        if (isAnimalContainsNewType) {
            throw new ConflictException(Message.ANIMAL_TYPES_CONTAIN_NEW_ANIMAL_TYPE.getInfo());
        }

        animalTypes.remove(oldType);
        animalTypes.add(newType);
        animal.setAnimalTypes(animalTypes);
        return animal;
    }

    @Override
    public Animal deleteAnimalTypeInAnimal(@NotNull @Positive Long animalId, @NotNull @Positive Long typeId) {
        Animal animal = get(animalId);
        AnimalType animalType = animalTypeService.get(typeId);
        List<AnimalType> animalTypes = animal.getAnimalTypes();

        var isLastAnimalType = animalTypes.size() == 1 && animalTypes.getFirst().equals(animalType);
        if (isLastAnimalType) {
            throw new BadRequestException(Message.LAST_ANIMAL_TYPE.getInfo());
        }

        var isAnimalContainsType = animalTypes.contains(animalType);
        if (!isAnimalContainsType) {
            throw new NotFoundException(Message.ANIMAL_TYPE_NOT_FOUND.getInfo());
        }

        animalTypes.remove(animalType);
        animal.setAnimalTypes(animalTypes);
        return animalRepository.save(animal);
    }

    private boolean isDead(LifeStatus lifeStatus) {
        return lifeStatus.name().equals(LifeStatus.DEAD.name());
    }

    private List<AnimalType> getAnimalTypes(List<Long> animalTypesIds) throws NotFoundException {
        List<AnimalType> animalTypes = new ArrayList<>();
        animalTypesIds.forEach(id -> animalTypes.add(animalTypeService.get(id)));
        return animalTypes;
    }

    private boolean hasAnimalTypesDuplicates(List<Long> animalTypes) {
        return animalTypes
                .stream()
                .distinct()
                .count() < animalTypes.size();
    }
}
