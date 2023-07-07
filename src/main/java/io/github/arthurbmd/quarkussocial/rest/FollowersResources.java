package io.github.arthurbmd.quarkussocial.rest;

import io.github.arthurbmd.quarkussocial.domain.model.Follower;
import io.github.arthurbmd.quarkussocial.domain.model.User;
import io.github.arthurbmd.quarkussocial.domain.repository.FollowerRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.UserRepository;
import io.github.arthurbmd.quarkussocial.rest.dto.FollowerPerUserResponse;
import io.github.arthurbmd.quarkussocial.rest.dto.FollowerRequest;
import io.github.arthurbmd.quarkussocial.rest.dto.FollowerResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowersResources {

    private final FollowerRepository repository;
    private final UserRepository userRepository;

    @Inject
    public FollowersResources(UserRepository userRepository, FollowerRepository repository){
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){
        User user = userRepository.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<Follower> list = repository.findByUser(userId);

        FollowerPerUserResponse result = new FollowerPerUserResponse();
        result.setFollowersCount(list.size());

        List<FollowerResponse> followerList = list.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());

        result.setContent(followerList);

        return Response.ok(result).build();
    }


    @PUT
    @Transactional
    public Response followUser(@PathParam("userId") Long userId, FollowerRequest followerRequest) {

        if (userId.equals(followerRequest.getFollowerId())) {
            return Response.status(Response.Status.CONFLICT).entity("You can't follow yourself").build();
        }
        User user = userRepository.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        User follower = userRepository.findById(followerRequest.getFollowerId());

        boolean follows = repository.follows(follower, user);

        if (!follows) {
            Follower entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);

            repository.persist(entity);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {

        User user = userRepository.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        repository.deleteByFollowerAndUser(followerId, userId);

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
