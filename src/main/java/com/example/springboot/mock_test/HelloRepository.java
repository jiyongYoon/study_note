package com.example.springboot.mock_test;

import org.springframework.stereotype.Repository;

@Repository
public class HelloRepository {

    public String save(String data) {
        System.out.println("HelloRepository save(), data = " + data);
        return data;
    }

    public String findById(String data) {
        System.out.println("HelloRepository findById(), data = " + data);
        return data;
    }
}
