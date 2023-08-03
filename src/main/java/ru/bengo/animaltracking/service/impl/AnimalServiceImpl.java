package ru.bengo.animaltracking.service.impl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.bengo.animaltracking.dto.AnimalDto;
import ru.bengo.animaltracking.dto.TypeDto;
import ru.bengo.animaltracking.exception.*;
import ru.bengo.animaltracking.model.*;
import ru.bengo.animaltracking.repository.AccountRepository;
import ru.bengo.animaltracking.repository.AnimalRepository;
import ru.bengo.animaltracking.repository.LocationRepository;
import ru.bengo.animaltracking.service.AnimalService;
import ru.bengo.animaltracking.service.AnimalTypeService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;
    private final LocationRepository locationRepository;
    private final AccountRepository accountRepository;
    private final AnimalTypeService animalTypeService;

    private static final Logger log = LoggerFactory.getLogger(AnimalServiceImpl.class);

    @Override
    public Animal create(@Valid AnimalDto animalDto) throws AnimalTypesHasDuplicatesException,
            AnimalTypeNotFoundException, ChipperIdNotFoundException, ChippingLocationIdNotFound {

        var animalTypesIds = animalDto.animalTypes();
        var animalTypes = getAnimalTypes(animalTypesIds);

        List<Long> visitedLocationsIds = new ArrayList<>();
        visitedLocationsIds.add(animalDto.chippingLocationId());
        var visitedLocations = getVisitedLocations(visitedLocationsIds);

        var chipperId = animalDto.chipperId();
        var chippingLocationId = animalDto.chippingLocationId();
        log.warn("iam here");

        if (hasAnimalTypesDuplicates(animalTypesIds)) {
            throw new AnimalTypesHasDuplicatesException(Message.ANIMAL_TYPES_HAS_DUPLICATES.getInfo());
        }
        if (!isChipperIdExist(chipperId)) {
            throw new ChipperIdNotFoundException(Message.CHIPPER_ID_NOT_FOUND.getInfo());
        }
        if (!isChippingLocationExist(chippingLocationId)) {
            throw new ChippingLocationIdNotFound(Message.CHIPPING_LOCATION_ID_NOT_FOUND.getInfo());
        }

        log.warn(">> create animal");
        Animal animal = convertToEntity(animalDto, animalTypes, visitedLocations);
        return animalRepository.save(animal);
    }

    @Override
    public Animal get(@NotNull @Positive Long id) throws AnimalNotFoundException {
        Optional<Animal> foundAnimal = animalRepository.findById(id);

        if (foundAnimal.isEmpty()) {
            throw new AnimalNotFoundException(Message.ANIMAL_NOT_FOUND.getInfo());
        }

        return foundAnimal.get();
    }


    @Override
    public Animal update(@NotNull @Positive Long id, @Valid AnimalDto animalDto) throws AnimalNotFoundException,
            UpdateDeadToAliveException, ChipperIdNotFoundException, ChippingLocationIdNotFound,
            NewChippingLocationIdEqualsFirstVisitedLocationIdException {

        var foundOptionalAnimal = animalRepository.findById(id);
        if (foundOptionalAnimal.isEmpty()) {
            throw new AnimalNotFoundException(Message.ANIMAL_NOT_FOUND.getInfo());
        }

        var foundAnimal = foundOptionalAnimal.get();
        var requestChipperId = animalDto.chipperId();
        var requestChippingLocationId = animalDto.chippingLocationId();
        var requestLifeStatusRequest = animalDto.lifeStatus();
        var requestFirstVisitedLocationId = foundAnimal.getVisitedLocationsJson().get(0);

        if (isDead(foundAnimal.getLifeStatus())) {
            if (requestLifeStatusRequest.equals(LifeStatus.ALIVE.name())) {
                throw new UpdateDeadToAliveException(Message.UPDATE_DEAD_TO_ALIVE.getInfo());
            }
        }
        if (!isChipperIdExist(requestChipperId)) {
            throw new ChipperIdNotFoundException(Message.CHIPPER_ID_NOT_FOUND.getInfo());
        }
        if (!isChippingLocationExist(requestChippingLocationId)) {
            throw new ChippingLocationIdNotFound(Message.CHIPPING_LOCATION_ID_NOT_FOUND.getInfo());
        }
        if (isNewChippingLocationIdEqualsFirstVisitedLocationId(requestChippingLocationId,
                requestFirstVisitedLocationId)) {
            throw new NewChippingLocationIdEqualsFirstVisitedLocationIdException(
                    Message.NEW_CHIPPING_LOCATION_ID_EQUALS_FIRST_VISITED_LOCATION.getInfo());
        }

        //Animal animal = convertToEntity(animalDto);
        return animalRepository.save(new Animal());
    }

    @Override
    public void delete(@NotNull @Positive Long id) throws AnimalNotFoundException {

        var foundOptionalAnimal = animalRepository.findById(id);
        if (foundOptionalAnimal.isEmpty()) {
            throw new AnimalNotFoundException(Message.ANIMAL_NOT_FOUND.getInfo());
        }

        animalRepository.deleteById(id);
    }

    @Override
    public List<Animal> search(LocalDateTime startDateTime, LocalDateTime endDateTime,
                               @Positive Integer chipperId, @Positive Long chippingLocationId,
                               String lifeStatus, String gender,
                               @Min(0) Integer from, @Min(1) Integer size) {

        PageRequest pageRequest = PageRequest.of(from, size);

        return animalRepository.search(startDateTime, endDateTime, chipperId,
                chippingLocationId, lifeStatus, gender, pageRequest);
    }

    @Override
    public Animal addAnimalTypeToAnimal(@NotNull @Positive Long animalId,
                                        @NotNull @Positive Long typeId) throws AnimalNotFoundException,
            AnimalTypeNotFoundException, AnimalTypesContainNewAnimalTypeException {

        var foundOptionalAnimal = animalRepository.findById(animalId);
        if (foundOptionalAnimal.isEmpty()) {
            throw new AnimalNotFoundException(Message.ANIMAL_NOT_FOUND.getInfo());
        }

        Animal foundAnimal = foundOptionalAnimal.get();
        var animalTypes = foundAnimal.getAnimalTypesJson();
        if (isAnimalTypesContainNewAnimalTypeId(foundAnimal.getAnimalTypesJson(), typeId)) {
            throw new AnimalTypesContainNewAnimalTypeException(Message.ANIMAL_TYPES_CONTAIN_NEW_ANIMAL_TYPE.getInfo());
        }

        animalTypes.add(animalTypeService.get(typeId).getId());
        // foundAnimal.setAnimalTypes(animalTypes);
        return animalRepository.save(foundAnimal);
    }

    @Override
    public Animal updateAnimalTypesInAnimal(@NotNull @Positive Long animalId,
                                            @Valid TypeDto typeDto) throws AnimalNotFoundException,
            AnimalTypeNotFoundException, AnimalDoesNotHaveTypeException, AnimalTypeAlreadyExist {

        Optional<Animal> foundOptionalAnimal = animalRepository.findById(animalId);
        if (foundOptionalAnimal.isEmpty()) {
            throw new AnimalNotFoundException(Message.ANIMAL_NOT_FOUND.getInfo());
        }

        Long newTypeId = typeDto.newTypeId();
        Long oldTypeId = typeDto.oldTypeId();
        animalTypeService.get(newTypeId);
        animalTypeService.get(oldTypeId);
        if (!isAnimalTypeExist(oldTypeId)) {
            throw new AnimalDoesNotHaveTypeException(Message.ANIMAL_DOES_NOT_HAVE_TYPE.getInfo());
        }
        if (isAnimalTypeExist(newTypeId)) {
            throw new AnimalTypeAlreadyExist(Message.ANIMAL_TYPE_EXIST.getInfo());
        }
        if (isAnimalTypesExist(List.of(newTypeId, oldTypeId))) {
            throw new AnimalTypeAlreadyExist(Message.ANIMAL_TYPES_EXISTS_WITH_NEW_OLD_TYPES.getInfo());
        }

        Animal foundAnimal = foundOptionalAnimal.get();
        List<Long> animalTypes = foundAnimal.getAnimalTypesJson();
//        animalTypes.;
//        animalTypes.add(newTypeId)
        return null;
    }

    private Animal convertToEntity(AnimalDto animalDto, List<AnimalType> animalTypes, List<Location> visitedLocations) {
        List<Long> visitedLocationsJson = new ArrayList<>();
        visitedLocationsJson.add(animalDto.chippingLocationId());

        return Animal.builder()
                .animalTypes(animalTypes)
                .weight(animalDto.weight())
                .length(animalDto.length())
                .height(animalDto.height())
                .gender(Gender.valueOf(animalDto.gender()))
                .chipperId(animalDto.chipperId())
                .chippingLocationId(animalDto.chippingLocationId())
                .visitedLocations(visitedLocations)
                .build();
    }

    private List<AnimalType> getAnimalTypes(List<Long> animalTypesIds) throws AnimalTypeNotFoundException {
        List<AnimalType> animalTypes = new ArrayList<>();
        for (Long id : animalTypesIds) {
            AnimalType animalType = animalTypeService.get(id);
            animalTypes.add(animalType);
        }
        return animalTypes;
    }

    private List<Location> getVisitedLocations(List<Long> visitedLocationsIds) {
        List<Location> visitedLocations = new ArrayList<>();
        for (Long id: visitedLocationsIds) {
            Location visitedLocation;
        }
        return visitedLocations;
    }

    private boolean isDead(LifeStatus lifeStatus) {
        return lifeStatus.name().equals(LifeStatus.DEAD.name());
    }

    private boolean isAnimalTypesExist(List<Long> animalTypes) throws AnimalTypeNotFoundException {
        for (var id: animalTypes) {
            var foundAnimalType = animalTypeService.get(id);
        }
        return true;
    }

    private boolean isAnimalTypeExist(Long animalType) throws AnimalTypeNotFoundException {
        return isAnimalTypesExist(List.of(animalType));
    }

    private boolean isChipperIdExist(Integer id) {
        return accountRepository.findById(id).isPresent();
    }

    private boolean isChippingLocationExist(Long id) {
        return locationRepository.findById(id).isPresent();
    }

    private boolean hasAnimalTypesDuplicates(List<Long> animalTypes) {
        return animalTypes.stream().distinct().count() < animalTypes.size();
    }

    private boolean isNewChippingLocationIdEqualsFirstVisitedLocationId(Long newChippingLocationId,
                                                                        Long firstVisitedLocationId) {
        return newChippingLocationId.equals(firstVisitedLocationId);
    }

    private boolean isAnimalTypesContainNewAnimalTypeId(List<Long> animalTypes, Long newAnimalType) {
        for (var animalType: animalTypes) {
            if (animalType.equals(newAnimalType)) {
                return true;
            }
        }
        return false;
    }
}
