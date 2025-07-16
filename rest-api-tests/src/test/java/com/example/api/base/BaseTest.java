package com.example.api.base;

import com.example.api.config.TestConfig;
import com.example.api.listeners.TestListener;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Listeners(TestListener.class)
public class BaseTest {
    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);
    // Use a thread-safe list for parallel test safety
    protected static final List<String> testUserIds = Collections.synchronizedList(new ArrayList<>());
    protected static RequestSpecification requestSpec;

    public BaseTest() {
        // Default constructor for TestNG
    }

    @BeforeSuite(alwaysRun = true)
    public void setup() {
        try {
            log.info("Initializing test suite setup...");

            // Validate configuration
            validateConfig();

            // Set base URI
            RestAssured.baseURI = TestConfig.BASE_URI;
            log.info("Set base URI to: {}", TestConfig.BASE_URI);

            // Create request specification
            requestSpec = new RequestSpecBuilder()
                    .setContentType(ContentType.JSON)
                    .setAccept(ContentType.JSON)
                    .build();

            // Configure RestAssured logging if needed
            if (TestConfig.ENABLE_REQUEST_LOGGING) {
                RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
            }

            log.info("Test suite setup completed successfully");
        } catch (Exception e) {
            String errorMessage = "Failed to initialize test suite: " + e.getMessage();
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    private void validateConfig() {
        if (TestConfig.BASE_URI == null || TestConfig.BASE_URI.trim().isEmpty()) {
            throw new IllegalStateException("BASE_URI is not configured");
        }
        // Add more configuration validations as needed
        log.info("Configuration validation completed");
    }

    protected void addUserIdForCleanup(String userId) {
        if (userId != null && !userId.trim().isEmpty() && !testUserIds.contains(userId)) {
            testUserIds.add(userId);
            log.debug("Added user ID to cleanup list: {}", userId);
        }
    }

    protected static RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            throw new IllegalStateException("RequestSpecification has not been initialized. Ensure setup() is called.");
        }
        return requestSpec;
    }
}
