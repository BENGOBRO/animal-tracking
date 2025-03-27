package ru.bengo.animaltracking.api.dto.mapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.stereotype.Component;
import ru.bengo.animaltracking.api.dto.VisitedLocationDto;
import ru.bengo.animaltracking.store.entity.VisitedLocation;

@Component
@RequiredArgsConstructor
public class VisitedLocationMapper {

    private final ModelMapper modelMapper;

    public VisitedLocationDto toDto(VisitedLocation visitedLocation) {
        return modelMapper.map(visitedLocation, VisitedLocationDto.class);
    }

    @PostConstruct
    public void setupMapper() {
        TypeMap<VisitedLocation, VisitedLocationDto> propertyMapperAnimalLocation = modelMapper.createTypeMap(VisitedLocation.class, VisitedLocationDto.class);
        propertyMapperAnimalLocation.addMappings(
                x -> x.map(src -> src.getLocation().getId(), VisitedLocationDto::setLocationPointId)
        );
    }
}
