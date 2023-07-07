package io.github.arthurbmd.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePostRequest {

    @NotNull(message = "Text is required")
    private String text;
}
