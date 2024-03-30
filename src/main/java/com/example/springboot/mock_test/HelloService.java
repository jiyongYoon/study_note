package com.example.springboot.mock_test;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelloService {

    private final HelloRepository helloRepository;
    private final HelloClient helloClient;


    public String save() {
        System.out.println("HelloService save()");

        String data = helloClient.getData();
        String saveData = helloRepository.save(data);
        System.out.println("HelloService saveData = " + saveData);

        return saveData;
    }

}
