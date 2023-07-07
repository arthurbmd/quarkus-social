package io.github.arthurbmd.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {

    @NotBlank(message = "Name is require")
    private String name;
    @NotNull(message = "Age is Required")
    private Integer age;

}
