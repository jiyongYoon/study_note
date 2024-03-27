package com.example.validate;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.*;

@Getter
@ToString
public class CreateDto {

    @Null
    private Long id;

    @Email
    private String email;

    @NotBlank
    private String name;

    private String description;

}
