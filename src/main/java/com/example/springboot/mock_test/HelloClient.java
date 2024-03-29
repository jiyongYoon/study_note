package com.example.springboot;

import org.springframework.stereotype.Component;

@Component
public class HelloClient {

    public String getData() {
        String data = "HelloClient 실제 객체";
        System.out.println("HelloClient getData(), data = " + data);
        return data;
    }

}
