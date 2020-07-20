package com.cars.app.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link com.cars.app.domain.Car} entity. This class is used
 * in {@link com.cars.app.web.rest.CarResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /cars?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class CarCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter make;

    private StringFilter model;

    private DoubleFilter price;

    public CarCriteria() {
    }

    public CarCriteria(CarCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.make = other.make == null ? null : other.make.copy();
        this.model = other.model == null ? null : other.model.copy();
        this.price = other.price == null ? null : other.price.copy();
    }

    @Override
    public CarCriteria copy() {
        return new CarCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getMake() {
        return make;
    }

    public void setMake(StringFilter make) {
        this.make = make;
    }

    public StringFilter getModel() {
        return model;
    }

    public void setModel(StringFilter model) {
        this.model = model;
    }

    public DoubleFilter getPrice() {
        return price;
    }

    public void setPrice(DoubleFilter price) {
        this.price = price;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CarCriteria that = (CarCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(make, that.make) &&
            Objects.equals(model, that.model) &&
            Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        make,
        model,
        price
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CarCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (make != null ? "make=" + make + ", " : "") +
                (model != null ? "model=" + model + ", " : "") +
                (price != null ? "price=" + price + ", " : "") +
            "}";
    }

}
