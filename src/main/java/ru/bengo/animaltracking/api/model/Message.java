package ru.bengo.animaltracking.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Message {
    ACCOUNT_EXIST("There is an account with that email address"),
    LOCATION_EXIST("There is a location with that latitude and longitude"),
    ANIMAL_TYPE_EXIST("There is an animal type with that type"),
    ANIMAL_TYPES_HAS_DUPLICATES("Animal types array contains duplicates"),
    ANIMAL_TYPE_NOT_FOUND("Animal type not found"),
    ANIMAL_NOT_FOUND("Animal not found"),
    UPDATE_DEAD_TO_ALIVE("This animal is already dead"),
    NEW_CHIPPING_LOCATION_ID_EQUALS_FIRST_VISITED_LOCATION("New chipping location id equals first visited location id"),
    NO_ACCESS("No rights to access"),
    ACCOUNT_NOT_FOUND("Account with this id haven't found"),
    LOCATION_NOT_FOUND("Location with this id haven't found"),
    ACCOUNT_ASSOCIATION("Account is associated with an animal"),
    LOCATION_ASSOCIATION("Location is associated with an animal"),
    ANIMAL_ASSOCIATION("Animal is associated with visited locations"),
    ANIMAL_TYPE_ASSOCIATION("Animal is associated with animal type"),
    ANIMAL_TYPES_CONTAIN_NEW_ANIMAL_TYPE("The animal with typeId already has an animal type with animalId"),
    LAST_ANIMAL_TYPE("An animal has only one type and that is the type with typeId"),
    ANIMAL_IS_DEAD("The animal has lifeStatus = \"DEAD\""),
    CHIPPING_LOCATION_IS_VISITED_LOCATION("An attempt to add a location point equal to the chipping point."),
    ANIMAL_IN_CHIPPING_LOCATION("The animal is at the chipping point and has not moved anywhere"),
    ANIMAL_IS_ALREADY_LOCATED("An attempt to add a location point where an animal is already located"),
    ANIMAL_VISITED_LOCATION_NOT_FOUND("Animal visited location not found"),
    FIRST_VISITED_POINT_TO_CHIPPING_POINT("Updating the first visited point to the chipping point"),
    SAME_POINT("Updating a point to the same point"),
    NO_PASSWORD("No password")
    ;

    private final String info;

}
