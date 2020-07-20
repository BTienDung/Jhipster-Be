package com.cars.app.web.rest;

import com.cars.app.CarsappApp;
import com.cars.app.domain.Car;
import com.cars.app.repository.CarRepository;
import com.cars.app.service.CarService;
import com.cars.app.service.dto.CarCriteria;
import com.cars.app.service.CarQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CarResource} REST controller.
 */
@SpringBootTest(classes = CarsappApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CarResourceIT {

    private static final String DEFAULT_MAKE = "AAAAAAAAAA";
    private static final String UPDATED_MAKE = "BBBBBBBBBB";

    private static final String DEFAULT_MODEL = "AAAAAAAAAA";
    private static final String UPDATED_MODEL = "BBBBBBBBBB";

    private static final Double DEFAULT_PRICE = 1D;
    private static final Double UPDATED_PRICE = 2D;
    private static final Double SMALLER_PRICE = 1D - 1D;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private CarQueryService carQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCarMockMvc;

    private Car car;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createEntity(EntityManager em) {
        Car car = new Car()
            .make(DEFAULT_MAKE)
            .model(DEFAULT_MODEL)
            .price(DEFAULT_PRICE);
        return car;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Car createUpdatedEntity(EntityManager em) {
        Car car = new Car()
            .make(UPDATED_MAKE)
            .model(UPDATED_MODEL)
            .price(UPDATED_PRICE);
        return car;
    }

    @BeforeEach
    public void initTest() {
        car = createEntity(em);
    }

    @Test
    @Transactional
    public void createCar() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();
        // Create the Car
        restCarMockMvc.perform(post("/api/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(car)))
            .andExpect(status().isCreated());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate + 1);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getMake()).isEqualTo(DEFAULT_MAKE);
        assertThat(testCar.getModel()).isEqualTo(DEFAULT_MODEL);
        assertThat(testCar.getPrice()).isEqualTo(DEFAULT_PRICE);
    }

    @Test
    @Transactional
    public void createCarWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = carRepository.findAll().size();

        // Create the Car with an existing ID
        car.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCarMockMvc.perform(post("/api/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(car)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCars() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList
        restCarMockMvc.perform(get("/api/cars?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())));
    }
    
    @Test
    @Transactional
    public void getCar() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", car.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(car.getId().intValue()))
            .andExpect(jsonPath("$.make").value(DEFAULT_MAKE))
            .andExpect(jsonPath("$.model").value(DEFAULT_MODEL))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.doubleValue()));
    }


    @Test
    @Transactional
    public void getCarsByIdFiltering() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        Long id = car.getId();

        defaultCarShouldBeFound("id.equals=" + id);
        defaultCarShouldNotBeFound("id.notEquals=" + id);

        defaultCarShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCarShouldNotBeFound("id.greaterThan=" + id);

        defaultCarShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCarShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllCarsByMakeIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make equals to DEFAULT_MAKE
        defaultCarShouldBeFound("make.equals=" + DEFAULT_MAKE);

        // Get all the carList where make equals to UPDATED_MAKE
        defaultCarShouldNotBeFound("make.equals=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make not equals to DEFAULT_MAKE
        defaultCarShouldNotBeFound("make.notEquals=" + DEFAULT_MAKE);

        // Get all the carList where make not equals to UPDATED_MAKE
        defaultCarShouldBeFound("make.notEquals=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make in DEFAULT_MAKE or UPDATED_MAKE
        defaultCarShouldBeFound("make.in=" + DEFAULT_MAKE + "," + UPDATED_MAKE);

        // Get all the carList where make equals to UPDATED_MAKE
        defaultCarShouldNotBeFound("make.in=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make is not null
        defaultCarShouldBeFound("make.specified=true");

        // Get all the carList where make is null
        defaultCarShouldNotBeFound("make.specified=false");
    }
                @Test
    @Transactional
    public void getAllCarsByMakeContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make contains DEFAULT_MAKE
        defaultCarShouldBeFound("make.contains=" + DEFAULT_MAKE);

        // Get all the carList where make contains UPDATED_MAKE
        defaultCarShouldNotBeFound("make.contains=" + UPDATED_MAKE);
    }

    @Test
    @Transactional
    public void getAllCarsByMakeNotContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where make does not contain DEFAULT_MAKE
        defaultCarShouldNotBeFound("make.doesNotContain=" + DEFAULT_MAKE);

        // Get all the carList where make does not contain UPDATED_MAKE
        defaultCarShouldBeFound("make.doesNotContain=" + UPDATED_MAKE);
    }


    @Test
    @Transactional
    public void getAllCarsByModelIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model equals to DEFAULT_MODEL
        defaultCarShouldBeFound("model.equals=" + DEFAULT_MODEL);

        // Get all the carList where model equals to UPDATED_MODEL
        defaultCarShouldNotBeFound("model.equals=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model not equals to DEFAULT_MODEL
        defaultCarShouldNotBeFound("model.notEquals=" + DEFAULT_MODEL);

        // Get all the carList where model not equals to UPDATED_MODEL
        defaultCarShouldBeFound("model.notEquals=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model in DEFAULT_MODEL or UPDATED_MODEL
        defaultCarShouldBeFound("model.in=" + DEFAULT_MODEL + "," + UPDATED_MODEL);

        // Get all the carList where model equals to UPDATED_MODEL
        defaultCarShouldNotBeFound("model.in=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model is not null
        defaultCarShouldBeFound("model.specified=true");

        // Get all the carList where model is null
        defaultCarShouldNotBeFound("model.specified=false");
    }
                @Test
    @Transactional
    public void getAllCarsByModelContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model contains DEFAULT_MODEL
        defaultCarShouldBeFound("model.contains=" + DEFAULT_MODEL);

        // Get all the carList where model contains UPDATED_MODEL
        defaultCarShouldNotBeFound("model.contains=" + UPDATED_MODEL);
    }

    @Test
    @Transactional
    public void getAllCarsByModelNotContainsSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where model does not contain DEFAULT_MODEL
        defaultCarShouldNotBeFound("model.doesNotContain=" + DEFAULT_MODEL);

        // Get all the carList where model does not contain UPDATED_MODEL
        defaultCarShouldBeFound("model.doesNotContain=" + UPDATED_MODEL);
    }


    @Test
    @Transactional
    public void getAllCarsByPriceIsEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price equals to DEFAULT_PRICE
        defaultCarShouldBeFound("price.equals=" + DEFAULT_PRICE);

        // Get all the carList where price equals to UPDATED_PRICE
        defaultCarShouldNotBeFound("price.equals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsNotEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price not equals to DEFAULT_PRICE
        defaultCarShouldNotBeFound("price.notEquals=" + DEFAULT_PRICE);

        // Get all the carList where price not equals to UPDATED_PRICE
        defaultCarShouldBeFound("price.notEquals=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsInShouldWork() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price in DEFAULT_PRICE or UPDATED_PRICE
        defaultCarShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);

        // Get all the carList where price equals to UPDATED_PRICE
        defaultCarShouldNotBeFound("price.in=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsNullOrNotNull() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price is not null
        defaultCarShouldBeFound("price.specified=true");

        // Get all the carList where price is null
        defaultCarShouldNotBeFound("price.specified=false");
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price is greater than or equal to DEFAULT_PRICE
        defaultCarShouldBeFound("price.greaterThanOrEqual=" + DEFAULT_PRICE);

        // Get all the carList where price is greater than or equal to UPDATED_PRICE
        defaultCarShouldNotBeFound("price.greaterThanOrEqual=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price is less than or equal to DEFAULT_PRICE
        defaultCarShouldBeFound("price.lessThanOrEqual=" + DEFAULT_PRICE);

        // Get all the carList where price is less than or equal to SMALLER_PRICE
        defaultCarShouldNotBeFound("price.lessThanOrEqual=" + SMALLER_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsLessThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price is less than DEFAULT_PRICE
        defaultCarShouldNotBeFound("price.lessThan=" + DEFAULT_PRICE);

        // Get all the carList where price is less than UPDATED_PRICE
        defaultCarShouldBeFound("price.lessThan=" + UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void getAllCarsByPriceIsGreaterThanSomething() throws Exception {
        // Initialize the database
        carRepository.saveAndFlush(car);

        // Get all the carList where price is greater than DEFAULT_PRICE
        defaultCarShouldNotBeFound("price.greaterThan=" + DEFAULT_PRICE);

        // Get all the carList where price is greater than SMALLER_PRICE
        defaultCarShouldBeFound("price.greaterThan=" + SMALLER_PRICE);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCarShouldBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(car.getId().intValue())))
            .andExpect(jsonPath("$.[*].make").value(hasItem(DEFAULT_MAKE)))
            .andExpect(jsonPath("$.[*].model").value(hasItem(DEFAULT_MODEL)))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.doubleValue())));

        // Check, that the count call also returns 1
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCarShouldNotBeFound(String filter) throws Exception {
        restCarMockMvc.perform(get("/api/cars?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCarMockMvc.perform(get("/api/cars/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingCar() throws Exception {
        // Get the car
        restCarMockMvc.perform(get("/api/cars/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCar() throws Exception {
        // Initialize the database
        carService.save(car);

        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // Update the car
        Car updatedCar = carRepository.findById(car.getId()).get();
        // Disconnect from session so that the updates on updatedCar are not directly saved in db
        em.detach(updatedCar);
        updatedCar
            .make(UPDATED_MAKE)
            .model(UPDATED_MODEL)
            .price(UPDATED_PRICE);

        restCarMockMvc.perform(put("/api/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCar)))
            .andExpect(status().isOk());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
        Car testCar = carList.get(carList.size() - 1);
        assertThat(testCar.getMake()).isEqualTo(UPDATED_MAKE);
        assertThat(testCar.getModel()).isEqualTo(UPDATED_MODEL);
        assertThat(testCar.getPrice()).isEqualTo(UPDATED_PRICE);
    }

    @Test
    @Transactional
    public void updateNonExistingCar() throws Exception {
        int databaseSizeBeforeUpdate = carRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCarMockMvc.perform(put("/api/cars")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(car)))
            .andExpect(status().isBadRequest());

        // Validate the Car in the database
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCar() throws Exception {
        // Initialize the database
        carService.save(car);

        int databaseSizeBeforeDelete = carRepository.findAll().size();

        // Delete the car
        restCarMockMvc.perform(delete("/api/cars/{id}", car.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Car> carList = carRepository.findAll();
        assertThat(carList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
