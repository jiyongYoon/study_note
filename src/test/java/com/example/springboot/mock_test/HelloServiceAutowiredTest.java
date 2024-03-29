package com.example.springboot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HelloServiceAutowiredTest {

    @Autowired
    HelloService helloService;

    @Test
    void autowiredTest() {
        String data = helloService.save();

        Assertions.assertThat(data).isEqualTo("HelloClient 실제 객체");
    }
}
