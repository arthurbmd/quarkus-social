package io.github.arthurbmd.quarkussocial.rest;

import io.github.arthurbmd.quarkussocial.domain.model.Follower;
import io.github.arthurbmd.quarkussocial.domain.model.Post;
import io.github.arthurbmd.quarkussocial.domain.model.User;
import io.github.arthurbmd.quarkussocial.domain.repository.FollowerRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.PostRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.UserRepository;
import io.github.arthurbmd.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PostResources.class)
class PostResourcesTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;
    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp() {
        User user = new User();
        user.setAge(30);
        user.setName("Fulano");

        userRepository.persist(user);

        userId = user.getId();

        Post post = new Post();
        post.setText("Some Text");
        post.setUser(user);
        postRepository.persist(post);


        User userNotFollower = new User();
        userNotFollower.setAge(20);
        userNotFollower.setName("Cicrano");
        userRepository.persist(userNotFollower);

        userNotFollowerId = userNotFollower.getId();

        User userFollower = new User();
        userFollower.setAge(15);
        userFollower.setName("Beltrano");
        userRepository.persist(userFollower);

        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);

    }

    @Test
    @DisplayName("should creat a post for a user")
    public void createPostTest(){
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("some text");

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(postRequest))
                .pathParam("userId", userId)
                .when()
                .post()
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Should return 404 when trying to make a post for a nonexistent user")
    public void createPostErrorTest(){
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("some text");

        int nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(postRequest))
                .pathParam("userId", nonexistentUserId)
                .when()
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest() {

        int nonexistentUserId = 999;

        given()
                .pathParam("userId", nonexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(404);

    }

    @Test
    @DisplayName("should return 400 when followerId is not present")
    public void listPostFollowerHeaderNotSentTest() {

        given()
                .pathParam("userId", userId)
                .when()
                .get()
                .then().statusCode(400)
                .body(Matchers.is("You've forgotten the header followerId"));

    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest() {

        int nonexistentUserId = 999;

        given()
                .pathParam("userId", userId)
                .headers("followerId", nonexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(400)
                .body(Matchers.is("Nonexistent followerId"));

    }

    @Test
    @DisplayName("should return 403 when follower doesn't follow user")
    public void listPostNotAFollowerTest() {
        given()
                .pathParam("userId", userId)
                .headers("followerId", userNotFollowerId)
                .when()
                .get()
                .then()
                .statusCode(403)
                .body(Matchers.is("You can't see these posts"));


    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest() {

        given()
                .pathParam("userId", userId)
                .headers("followerId", userFollowerId)
                .when()
                .get()
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));


    }
}