package com.example.java.static_factory_method;

public class Main {

    public static void main(String[] args) {
        Car avante = Car.of("Hyundai", "AVANTE", 2021);
        System.out.println(avante);

        Car thisYearAvante = Car.ofThisYear("Hyundai","AVANTE");
        System.out.println(thisYearAvante);

        Car thisYearTeslaCar = TeslaCar.ofThisYear("ModelS");
        System.out.println(thisYearTeslaCar);

        TeslaCar thisYearModelY = TeslaCar.getCachedCarOfThisYear("ModelY");
        System.out.println(thisYearModelY);
        System.out.println("thisYearModelY.hashCode() = " + thisYearModelY.hashCode()); // 동일한 객체

        TeslaCar thisYearModelY2 = TeslaCar.getCachedCarOfThisYear("ModelY");
        System.out.println(thisYearModelY2);
        System.out.println("thisYearModelY2.hashCode() = " + thisYearModelY2.hashCode()); // 동일한 객체
    }
}
