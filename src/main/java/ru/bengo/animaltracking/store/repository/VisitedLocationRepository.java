package ru.bengo.animaltracking.store.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.bengo.animaltracking.store.entity.VisitedLocation;

import java.util.Date;
import java.util.List;

@Repository
public interface VisitedLocationRepository extends CrudRepository<VisitedLocation, Long> {

    @Query(value =
            "select * from animal_visited_location " +
            "where (CAST(?1 AS timestamp) is null or date_time_of_visit_location_point > CAST(?1 AS timestamp)) " +
            "and (CAST(?2 AS timestamp) is null or date_time_of_visit_location_point < CAST(?2 AS timestamp)) " +
            "order by id", nativeQuery = true)
    List<VisitedLocation> search(Date startDateTime, Date endDateTime);

}
