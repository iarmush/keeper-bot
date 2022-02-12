package ru.my.bot;

import static io.restassured.RestAssured.given;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import ru.my.bot.controller.BotController;

@QuarkusTest
@TestHTTPEndpoint(BotController.class)
public class BotControllerTest {

    @Test
    public void testDownloadZip() {
        given()
            .when().get("/api/keeper-bot/download?chatId=123")
            .then()
            .statusCode(500);
    }
}