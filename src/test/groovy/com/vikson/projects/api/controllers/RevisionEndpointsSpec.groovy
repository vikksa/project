package com.vikson.projects.api.controllers

import io.restassured.RestAssured
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static io.restassured.RestAssured.given

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(["integration"])
class RevisionEndpointsSpec extends Specification {

    @LocalServerPort int port

    def setup() {
        RestAssured.port = port
    }

    def 'A Revision should include a Pre-Signed URL for Plan Archive Downloads.'() {
        given:
        def request = given()
                .auth().oauth2(token)
                .log().all()
        when:
        def response = request.get("/api/v2/plans/${planId}/revision")
        then:
        String downloadURL = response.then()
                .log().all()
                .statusCode(200)
                .extract()
                .body().path("archiveDownloadUrl")
        getStatusCode(downloadURL) == 200
        where:
        planId | token
        "86128567-9536-4fea-a5a1-9f41f052ccaa" | "token"
    }

    def 'A Revision should include a Pre-Signed URL for an original plan file.'() {
        given:
        def request = given()
                .auth().oauth2(token)
                .log().all()
        when:
        def response = request.get("/api/v2/plans/${planId}/revision")
        then:
        String downloadURL = response.then()
                .log().all()
                .statusCode(200)
                .extract()
                .body().path("originalDownloadUrl")
        getStatusCode(downloadURL) == 200
        where:
        planId | token
        "f38ffb0f-306a-4053-a732-44146be9fd06" | "token"
    }

    private int getStatusCode(String url) {
        HttpURLConnection.setFollowRedirects(false)
        def conn = new URL(url).openConnection() as HttpURLConnection
        conn.setRequestMethod('GET')
        conn.connect()
        return conn.getResponseCode()
    }

}