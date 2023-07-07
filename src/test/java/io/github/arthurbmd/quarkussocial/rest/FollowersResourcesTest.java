package io.github.arthurbmd.quarkussocial.rest;

import io.github.arthurbmd.quarkussocial.domain.model.Follower;
import io.github.arthurbmd.quarkussocial.domain.model.User;
import io.github.arthurbmd.quarkussocial.domain.repository.FollowerRepository;
import io.github.arthurbmd.quarkussocial.domain.repository.UserRepository;
import io.github.arthurbmd.quarkussocial.rest.dto.FollowerRequest;
import io.restassured.response.Response;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response.Status;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowersResources.class)
class FollowersResourcesTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {

        User user = new User();
        user.setName("Fulano");
        user.setAge(30);
        userRepository.persist(user);
        userId = user.getId();

        User follower = new User();
        follower.setName("Cicrano");
        follower.setAge(25);
        userRepository.persist(follower);
        followerId = follower.getId();

        Follower followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);

    }

    @Test
    @DisplayName("Should return 409 when followerId is equal to userId")

    public void sameUserAsFollowerTest() {

        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(userId);

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can't follow yourself"));

    }

    @Test
    @DisplayName("Should return 404 when trying to follow nonexistent User")
    public void followUserNotFoundTest() {

        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(userId);

        int nonexistentId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .pathParam("userId", nonexistentId)
                .when()
                .put()
                .then()
                .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should follow a user")
    public void followUserTest() {
        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .pathParam("userId", userId)
                .when()
                .put()
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("Should return 404 when trying to list nonexistent User's followers")
    public void listUserNotFoundTest() {
        int nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParam("userId", nonexistentUserId)
                .when()
                .get()
                .then()
                .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("Should return User's followers")
    public void listFollowersTest() {

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParam("userId", userId)
                .when()
                .get()
                .then()
                .extract().response();

        Integer followersCount = response.jsonPath().get("followersCount");
        List<Object> followersContent = response.jsonPath().getList("content");
        assertEquals(Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());

    }

    @Test
    @DisplayName("should return 404 on unfollow nonexistent userId")
    public void unfollowUserNotFoundTest() {

        int nonexistentUserId = 999;

        given()
                .pathParam("userId", nonexistentUserId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should unfollow an user")
    public void unfollowUser(){
        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
                .when()
                .delete()
                .then()
                .statusCode(Status.NO_CONTENT.getStatusCode());
    }
}