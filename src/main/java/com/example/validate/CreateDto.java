package com.example.validate;

import com.example.validate.custom.Create;
import com.example.validate.custom.Password;
import lombok.Getter;
import lombok.ToString;

import jakarta.validation.constraints.*;

@Getter
@ToString
public class CreateDto {

    @Null
    private Long id;

    @Email
    private String email;

    @Size(min = 5)
    @Password(groups = Create.class)
    private String password;

    @NotBlank
    private String name;

    private String description;

}
