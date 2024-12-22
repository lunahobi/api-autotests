import io.qameta.allure.Story;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.junit.Test;
import pojos.*;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReqresTest {

    @Test
    @Story("Positive")
    @DisplayName("Успешная регистрация")
    public void registerSuccessful() {
        LoginRegisterRequest rq =
                LoginRegisterRequest.builder()
                        .email("eve.holt@reqres.in")
                        .password("pistol")
                        .build();

        LoginRegisterResponse rs = given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(rq)
                .when()
                .post("https://reqres.in/api/register")
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("RegisterSuccessfulResponseSchema.json"))
                .extract().as(LoginRegisterResponse.class);

        assertThat(rs)
                .isNotNull()
                .extracting(LoginRegisterResponse::getId)
                .isEqualTo(4);

        assertThat(rs)
                .isNotNull()
                .extracting(LoginRegisterResponse::getToken)
                .isEqualTo("QpwL5tke4Pnpja7X4");
    }

    @Test
    @Story("Negative")
    @DisplayName("Неуспешная регистрация")
    public void registerUnsuccessful() {
        LoginRegisterRequest rq =
                LoginRegisterRequest.builder()
                        .email("sydney@fife")
                        .build();

        LoginRegisterResponse rs = given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(rq)
                .when()
                .post("https://reqres.in/api/register")
                .then()
                .statusCode(400)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("RegisterLoginUnsuccessfulResponseSchema.json"))
                .extract().as(LoginRegisterResponse.class);
        assertThat(rs)
                .isNotNull()
                .extracting(LoginRegisterResponse::getError)
                .isEqualTo("Missing password");
    }

    @Test
    @Story("Positive")
    @DisplayName("Обновить пользователя PUT")
    public void updateUserPut() {
        UserRequest rq =
                UserRequest.builder()
                        .name("morpheus")
                        .job("zion resident")
                        .build();

        UserResponse rs = given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(rq)
                .when()
                .put("https://reqres.in/api/users/2")
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("UpdateUserResponseSchema.json"))
                .extract().as(UserResponse.class);

        assertThat(rs)
                .isNotNull()
                .extracting(UserResponse::getName)
                .isEqualTo(rq.getName());

        assertThat(rs)
                .isNotNull()
                .extracting(UserResponse::getJob)
                .isEqualTo(rq.getJob());
    }

    @Test
    @Story("Negative")
    @DisplayName("Обновить пользователя PUT с неправильным форматом body")
    public void updateUserPutWrongBody() {
        Response response =  given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .body(" ")
                .when()
                .put("https://reqres.in/api/users/2");

        response.then().statusCode(400);

        String responseBody = response.getBody().asString();

        XmlPath xmlPath = new XmlPath(XmlPath.CompatibilityMode.HTML, responseBody);
        assertEquals("Error", xmlPath.getString("html.head.title"));
        assertEquals("Bad Request", xmlPath.getString("html.body.pre"));
    }

    @Test
    @Story("Positive")
    @DisplayName("Удалить пользователя")
    public void deleteUser() {
        given().
                when()
                .delete("https://reqres.in/api/users/2")
                .then()
                .statusCode(204)
                .body(equalTo(""));
    }

    @Test
    @Story("Positive")
    @DisplayName("Получить пользователя с id=2")
    public void getUser() {
        UserData user = given()
                .filter(new AllureRestAssured())
                .when()
                .get("https://reqres.in/api/users/2")
                .then()
                .statusCode(200)
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("UserSingleSchema.json"))
                .extract().jsonPath().getObject("data", UserData.class);
        assertThat(user).extracting(UserData::getId).isEqualTo(2);
        assertThat(user).extracting(UserData::getEmail).isEqualTo("janet.weaver@reqres.in");
        assertThat(user).extracting(UserData::getFirst_name).isEqualTo("Janet");
        assertThat(user).extracting(UserData::getLast_name).isEqualTo("Weaver");
        assertThat(user).extracting(UserData::getAvatar).isEqualTo("https://reqres.in/img/faces/2-image.jpg");
    }

    @Test
    @Story("Negative")
    @DisplayName("Получить пользователя с id=-1")
    public void getUserNotFound() {
        given()
                .filter(new AllureRestAssured())
                .when()
                .get("https://reqres.in/api/users/-1")
                .then()
                .statusCode(404)
                .body(equalTo("{}"))
                .extract().jsonPath().getObject("data", UserData.class);
    }


}
