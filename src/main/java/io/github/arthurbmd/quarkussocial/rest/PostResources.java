package io.github.arthurbmd.quarkussocial.rest;

import io.github.arthurbmd.quarkussocial.domain.model.Post;
import io.github.arthurbmd.quarkussocial.domain.model.User;
import io.github.arthurbmd.quarkussocial.domain.repository.FollowerRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.PostRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.UserRepository;
import io.github.arthurbmd.quarkussocial.rest.dto.CreatePostRequest;
import io.github.arthurbmd.quarkussocial.rest.dto.PostResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PostResources {

    private PostRepository repository;
    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public PostResources(UserRepository userRepository, PostRepository repository, FollowerRepository followerRepository) {
        this.userRepository = userRepository;
        this.repository = repository;
        this.followerRepository = followerRepository;
    }

    @GET
    public Response listAllPost(@PathParam("userId") Long userId,
                                @HeaderParam("followerId") Long followerId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (followerId == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("You've forgotten the header followerId")
                    .build();
        }

        User follower = userRepository.findById(followerId);
        if (follower == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Nonexistent followerId")
                    .build();
        }
        boolean follows = followerRepository.follows(follower, user);

        if (!follows) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("You can't see these posts")
                    .build();
        }

        PanacheQuery<Post> query = repository.find(
                "user", Sort.by("dateTime", Sort.Direction.Descending), user);

        List<Post> list = query.list();

        List<PostResponse> postResponseList = list.stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());

        return Response.ok(postResponseList).build();
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request) {

        User user = userRepository.findById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);

        repository.persist(post);
        return Response.status(Response.Status.CREATED).build();
    }
}
