package com.example.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ValidateController {

    private final ValidateService validateService;

    @PostMapping("/valid")
    public CreateDto valid(@Valid @RequestBody CreateDto createDto) {
        System.out.println("controller = " + createDto);
        return createDto;
    }

    @PostMapping("/validated")
    public CreateDto validate(@RequestBody CreateDto createDto) {
        System.out.println("controller = " + createDto);
        return validateService.validate(createDto);
    }

}
