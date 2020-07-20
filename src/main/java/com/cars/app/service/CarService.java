package com.cars.app.service;

import com.cars.app.domain.Car;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Car}.
 */
public interface CarService {

    /**
     * Save a car.
     *
     * @param car the entity to save.
     * @return the persisted entity.
     */
    Car save(Car car);

    /**
     * Get all the cars.
     *
     * @return the list of entities.
     */
    List<Car> findAll();


    /**
     * Get the "id" car.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Car> findOne(Long id);

    /**
     * Delete the "id" car.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
