package com.example.api.base;

import com.example.api.config.TestConfig;
import com.example.api.listeners.TestListener;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest {
    protected static RequestSpecification requestSpec;

    @BeforeSuite
    public void setup() {
        // Set base URI and port
        RestAssured.baseURI = TestConfig.BASE_URI;
        
        // Create request specification
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
}
