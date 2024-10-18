package com.example.validate;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;

@Service
@Validated
public class ValidateService {

    public CreateDto validate(@Valid CreateDto createDto) {
        System.out.println("service = " + createDto);
        return createDto;
    }
}
