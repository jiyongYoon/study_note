package com.example.validate;

import com.example.validate.custom.Create;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

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

    @PostMapping("/validated/group")
    public CreateDto validGroup(@Validated(Create.class) @RequestBody CreateDto createDto) {
        System.out.println("controller = " + createDto);
        return createDto;
    }
}
