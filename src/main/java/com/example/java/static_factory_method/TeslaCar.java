package com.example.java.static_factory_method;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TeslaCar extends Car {
    public TeslaCar(String model, int year) {
        super("Tesla", model, year);
    }

    public static TeslaCar of(String model, int year) {
        return new TeslaCar(model, year);
    }

    public static TeslaCar ofThisYear(String model) {
        return new TeslaCar(model, LocalDate.now().getYear());
    }

    private static final Map<String, TeslaCar> carCache2024 = new HashMap<>();

    /* 초기화 블럭에서는 static 변수를 초기화 해줄 수 있다 */
    static {
        carCache2024.put("ModelS", new TeslaCar("ModelS", LocalDate.now().getYear()));
        carCache2024.put("Model3", new TeslaCar("Model3", LocalDate.now().getYear()));
        carCache2024.put("ModelX", new TeslaCar("ModelX", LocalDate.now().getYear()));
        carCache2024.put("ModelY", new TeslaCar("ModelY", LocalDate.now().getYear()));
    }

    /** Map에 캐싱되어 있는 동일한 객체를 계속 반환한다. */
    public static TeslaCar getCachedCarOfThisYear(String model) {
        return carCache2024.get(model);
    }
}
