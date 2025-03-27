package ru.bengo.animaltracking.api.dto.mapper;

import org.mapstruct.*;
import ru.bengo.animaltracking.api.dto.AnimalTypeDto;
import ru.bengo.animaltracking.store.entity.AnimalType;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AnimalTypeMapper {

    AnimalTypeDto toDto(AnimalType animalType);

    AnimalType toEntity(AnimalTypeDto animalTypeDto);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(AnimalTypeDto animalTypeDto, @MappingTarget AnimalType animalType);
}
