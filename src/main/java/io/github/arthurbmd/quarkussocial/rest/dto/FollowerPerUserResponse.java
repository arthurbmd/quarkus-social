package io.github.arthurbmd.quarkussocial.rest.dto;

import io.github.arthurbmd.quarkussocial.domain.model.User;
import lombok.Data;

import java.util.List;

@Data
public class FollowerPerUserResponse {

    private Integer followersCount;
    private List<FollowerResponse> content;

}
