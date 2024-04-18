package com.example.java.static_factory_method;


import java.time.LocalDate;

public class Car {
    private String brand;
    private String model;
    private int year;

    public Car(String brand, String model, int year) {
        this.brand = brand;
        this.model = model;
        this.year = year;
    }

    public static Car of(String brand, String model, int year) {
        return new Car(brand, model, year);
    }

    /* 생성자 대신 사용하여 메서드 명에 의미를 담을 수 있다 */
    public static Car ofThisYear(String brand, String model) {
        return new Car(brand, model, LocalDate.now().getYear());
    }

    @Override
    public String toString() {
        return "Car{" +
                "brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                '}';
    }
}