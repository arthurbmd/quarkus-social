package io.github.arthurbmd.quarkussocial.rest;

import io.github.arthurbmd.quarkussocial.rest.dto.CreateUserRequest;
import io.github.arthurbmd.quarkussocial.rest.dto.ResponseError;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.json.bind.JsonbBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("should create an user successfully")
    @Order(1)
    public void createUserTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("Fulano");
        user.setAge(30);

        Response response = given()
                    .contentType(ContentType.JSON)
                    .body(JsonbBuilder.create().toJson(user))
                .when()
                    .post(apiURL)
                .then()
                    .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));

    }

    @Test
    @DisplayName("Should return error whe json is not valid")
    @Order(2)
    public void createUserValidationErrorTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setAge(null);
        user.setName(null);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(user))
                .when()
                .post(apiURL)
                .then()
                .extract()
                .response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY_STATUS, response.statusCode());
        assertEquals("Validation Error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));

    }



}