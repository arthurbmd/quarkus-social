package io.github.arthurbmd.quarkussocial.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowerRequest {

    @NotNull(message = "Follower is required")
    private Long followerId;
}
