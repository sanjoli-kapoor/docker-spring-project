package com.sanjoli.cucumber.steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportSteps {

    @LocalServerPort
    private int port;

    private Response response;

    @When("I POST to the import endpoint with filePath {string}")
    public void postWithFilePath(String filePath) throws Exception {
        String resolvedPath = filePath.startsWith("classpath:")
                ? resolveClasspathResource(filePath.replace("classpath:", ""))
                : filePath;

        response = RestAssured
                .given().baseUri("http://localhost").port(port)
                .when().post("/api/person/import?filePath=" + resolvedPath);
    }

    @When("I POST to the import endpoint without a filePath parameter")
    public void postWithoutFilePath() {
        response = RestAssured
                .given().baseUri("http://localhost").port(port)
                .when().post("/api/person/import");
    }

    @Then("the response status should be {int}")
    public void verifyStatus(int expectedStatus) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
    }

    @And("the response body should be {string}")
    public void verifyBodyEquals(String expectedBody) {
        assertThat(response.getBody().asString()).isEqualTo(expectedBody);
    }

    @And("the response body should contain {string}")
    public void verifyBodyContains(String expectedFragment) {
        assertThat(response.getBody().asString()).contains(expectedFragment);
    }

    private String resolveClasspathResource(String resourceName) throws Exception {
        URL resource = getClass().getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Classpath resource not found: " + resourceName);
        }
        return resource.getPath();
    }
}
