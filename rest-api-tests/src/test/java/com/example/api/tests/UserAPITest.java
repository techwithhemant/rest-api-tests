package com.example.api.tests;

import com.example.api.base.BaseTest;
import com.example.api.config.TestConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.restassured.specification.RequestSpecification;

public class UserAPITest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(UserAPITest.class);

    public UserAPITest() {
        super();
    }

    @BeforeClass(alwaysRun = true)
    public void init() {
        if (requestSpec == null) {
            setup();
        }
    }


    private void validateUserResponse(Response response, String expectedFirstName, String expectedLastName,
                                      int expectedGender, int expectedAgeBracket, String expectedPhone, String expectedEmail,
                                      Object expectedGroups) {
        if (response == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
        try {
            JsonPath jsonPath = response.jsonPath();
            assertThat("First name should match", jsonPath.getString("fN"), equalTo(expectedFirstName));
            assertThat("Last name should match", jsonPath.getString("lN"), equalTo(expectedLastName));
            assertThat("Gender should match", jsonPath.getInt("gender"), equalTo(expectedGender));
            assertThat("Age bracket should match", jsonPath.getInt("ageBracket"), equalTo(expectedAgeBracket));
            assertThat("Phone should match", jsonPath.getString("phone"), equalTo(expectedPhone));
            assertThat("Email should match", jsonPath.getString("email"), equalTo(expectedEmail));
            Object actualGroups = jsonPath.get("groups");
            assertThat("Groups should match", actualGroups, equalTo(expectedGroups));
            String userId = jsonPath.getString("id");
            assertThat("User ID should not be null", userId, notNullValue());
            String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
            assertThat("User ID should be a valid UUID", userId.matches(uuidPattern));
        } catch (Exception e) {
            log.error("Error validating user response: " + e.getMessage());
            throw new AssertionError("Failed to validate user response: " + e.getMessage(), e);
        }
    }

    @Test(description = "Verify that a new user can be created successfully with all required fields")
    public void testCreateFirstUserSuccessfully() {
        // Generate unique test data to avoid conflicts
        String uniqueId = java.util.UUID.randomUUID().toString();
        String uniquePhone = "1212" + System.currentTimeMillis() % 10000000;
        String uniqueEmail = "fn1.ln1." + System.currentTimeMillis() + "@success.com";
        String requestBody = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"" + uniquePhone + "\",\n" +
                "  \"email\": \"" + uniqueEmail + "\",\n" +
                "  \"id\": \"" + uniqueId + "\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";

        Response response = given()
                .spec(requestSpec)
                .body(requestBody)
            .when()
                .post(TestConfig.USERS_ENDPOINT)
            .then()
                .statusCode(201)
                .extract().response();

        log.info("Response: " + response.asString());

        // Add to cleanup
        String userId = response.jsonPath().getString("id");
        addUserIdForCleanup(userId);

        // Validate user response
        validateUserResponse(response, "FN1", "LN1", 1, 30, uniquePhone, uniqueEmail, null);
    }
    
    @Test(description = "Verify that a second user can be created with different details")
    public void testCreateSecondUserSuccessfully() {
        // Generate unique test data to avoid conflicts
        String uniqueId = java.util.UUID.randomUUID().toString();
        String uniquePhone = "1626" + System.currentTimeMillis() % 10000000;
        String uniqueEmail = "fn2.ln2." + System.currentTimeMillis() + "@success.com";
        String requestBody = "{\n" +
                "  \"fN\": \"FN2\",\n" +
                "  \"lN\": \"LN2\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"" + uniquePhone + "\",\n" +
                "  \"email\": \"" + uniqueEmail + "\",\n" +
                "  \"id\": \"" + uniqueId + "\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";
    
        Response response = given()
                .spec(requestSpec)
                .body(requestBody)
            .when()
                .post(TestConfig.USERS_ENDPOINT)
            .then()
                .statusCode(201)
                .extract().response();
                
                log.info("Response: " + response.asString());
    
    
        // Add to cleanup
        String userId = response.jsonPath().getString("id");
        addUserIdForCleanup(userId);
    
        // Validate user response
        validateUserResponse(response, "FN2", "LN2", 1, 30, uniquePhone, uniqueEmail, null);
    }
    

    @Test(description = "Verify that creating a user with duplicate ID or invalid phone fails")
    public void testCreateUserWithDuplicateIdOrInvalidPhone() {
        String duplicateIdRequest = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1111111111\",\n" +
                "  \"email\": \"another.email@success.com\",\n" +
                "  \"id\": \"" + TestConfig.TestUsers.ADMIN_USER_ID + "\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";

        Response duplicateIdResponse = given()
            .spec(requestSpec)
            .body(duplicateIdRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .extract().response();

        String duplicateIdErrorMessage = duplicateIdResponse.asString();
        assertThat(duplicateIdErrorMessage, not(is(emptyOrNullString())));
        // Accept both plain string and JSON error responses
        if (duplicateIdErrorMessage.trim().startsWith("{")) {
            // Try to extract error message from JSON
            try {
                JsonPath jsonPath = duplicateIdResponse.jsonPath();
                String title = jsonPath.getString("title");
                assertThat(title == null ? duplicateIdErrorMessage.toLowerCase() : title.toLowerCase(), containsString("already exists"));
            } catch (Exception e) {
                // fallback to string check
                assertThat(duplicateIdErrorMessage.toLowerCase(), containsString("already exists"));
            }
        } else {
            assertThat(duplicateIdErrorMessage.toLowerCase(), containsString("already exists"));
        }

        String invalidPhoneRequest = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"invalid-phone\",\n" +
                "  \"email\": \"valid.email@success.com\",\n" +
                "  \"id\": \"" + java.util.UUID.randomUUID().toString() + "\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";

        Response response = given()
            .spec(requestSpec)
            .body(invalidPhoneRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .extract().response();

        String errorMessage = response.asString();
        assertThat(errorMessage, not(is(emptyOrNullString())));
        // Accept both plain string and JSON error responses
        if (errorMessage.trim().startsWith("{")) {
            try {
                JsonPath jsonPath = response.jsonPath();
                String title = jsonPath.getString("title");
                String errors = jsonPath.getString("errors");
                String combined = (title == null ? "" : title) + " " + (errors == null ? "" : errors);
                assertThat(combined.toLowerCase(), anyOf(
                    containsString("invalid phone"),
                    containsString("already exists"),
                    containsString("user with this email already exists"),
                    containsString("user with this phone already exists")
                ));
            } catch (Exception e) {
                // fallback to string check
                assertThat(errorMessage.toLowerCase(), anyOf(
                    containsString("invalid phone"),
                    containsString("already exists"),
                    containsString("user with this email already exists"),
                    containsString("user with this phone already exists")
                ));
            }
        } else {
            assertThat(errorMessage.toLowerCase(), anyOf(
                containsString("invalid phone"),
                containsString("already exists"),
                containsString("user with this email already exists"),
                containsString("user with this phone already exists")
            ));
        }
    }

    @Test(description = "Verify that creating a user with invalid email fails")
    public void testCreateUserWithInvalidEmail() {
        String invalidEmailRequest = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1212121556\",\n" +
                "  \"email\": \"invalid-email\",\n" +
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000002\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";

        Response invalidEmailResponse = given()
            .spec(requestSpec)
            .body(invalidEmailRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .extract().response();

        String invalidEmailErrorMessage = invalidEmailResponse.asString();
        assertThat(invalidEmailErrorMessage, not(emptyOrNullString()));
    }
    
    @Test(description = "Verify that creating a user with duplicate email fails")
    public void testCreateUserWithDuplicateEmail() {
        String duplicateEmailRequest = "{\n" +
                "  \"fN\": \"FN_NEW\",\n" +
                "  \"lN\": \"LN_NEW\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"9998887776\",\n" +
                "  \"email\": \"" + TestConfig.TestUsers.ADMIN_EMAIL + "\",\n" +
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000003\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";
    
        Response response = given()
            .spec(requestSpec)
            .body(duplicateEmailRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .extract().response();
    
        String errorMessage = response.asString();
        assertThat(errorMessage, not(emptyOrNullString()));
    }
    
    // The following code block was misplaced and referenced an undefined 'response' variable.
    // If you want to add a test for duplicate phone, define 'duplicatePhoneRequest' and use a new Response variable as in other tests.

@Test(description = "Verify that creating a user with missing required fields fails")
public void testCreateUserWithMissingFields() {
    String invalidRequestBody = "{\n" +
            "  \"fN\": \"Test\",\n" +
            "  \"gender\": 1\n" + // Missing last name, email, and other required fields
            "}";

    Response response = given()
        .spec(requestSpec)
        .body(invalidRequestBody)
    .when()
        .post(TestConfig.USERS_ENDPOINT)
    .then()
        .statusCode(400)
        .extract().response();

    String errorMessage = response.asString();
    assertThat(errorMessage, not(emptyOrNullString()));
    // Accept both plain string and JSON error responses
    if (errorMessage.trim().startsWith("{")) {
        try {
            JsonPath jsonPath = response.jsonPath();
            String title = jsonPath.getString("title");
            String errors = jsonPath.getString("errors");
            String combined = (title == null ? "" : title) + " " + (errors == null ? "" : errors);
            assertThat(combined.toLowerCase(), anyOf(
                containsString("validation error"),
                containsString("field is required"),
                containsString("missing required fields"),
                containsString("not informed"),
                containsString("invalid input"),
                containsString("one or more validation errors occurred")
            ));
        } catch (Exception e) {
            // fallback to string check
            assertThat(errorMessage.toLowerCase(), anyOf(
                containsString("validation error"),
                containsString("field is required"),
                containsString("missing required fields"),
                containsString("not informed"),
                containsString("invalid input")
            ));
        }
    } else {
        assertThat(errorMessage.toLowerCase(), anyOf(
            containsString("validation error"),
            containsString("field is required"),
            containsString("missing required fields"),
            containsString("not informed"),
            containsString("invalid input")
        ));
    }
}

   @Test(description = "Verify that a non-existent user returns 404")
    public void testGetNonExistentUser() {
        String nonExistentId = "non-existent-id-123";
    
        Response response = given()
            .spec(requestSpec)
            .pathParam("id", nonExistentId)
        .when()
            .get(TestConfig.USERS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404)
            .extract().response();
    
        String errorMessage = response.asString();
        assertThat(errorMessage, not(emptyOrNullString()));
        assertThat(errorMessage.toLowerCase(), containsString("not found"));
    }

    @Test(description = "Verify that an existing user can be updated successfully")
    public void testUpdateUser() {
        // Updated user details with unique phone and email to avoid conflicts
        String uniqueId = java.util.UUID.randomUUID().toString();
        String updatedFirstName = "Hemant";
        String updatedLastName = "User";
        int updatedGender = 1;
        int updatedAgeBracket = 25;
        String updatedPhone = "77" + System.currentTimeMillis() % 10000000;
        String updatedEmail = "temp.user." + System.currentTimeMillis() + "@success.com";

        String createBody = "{\n" +
                "  \"fN\": \"Temp\",\n" +
                "  \"lN\": \"User\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 25,\n" +
                "  \"phone\": \"" + updatedPhone + "\",\n" +
                "  \"email\": \"" + updatedEmail + "\",\n" +
                "  \"id\": \"" + uniqueId + "\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}";

        // Create the user first
        given()
                .spec(requestSpec)
                .body(createBody)
            .when()
                .post(TestConfig.USERS_ENDPOINT)
            .then()
                .statusCode(201);

        String updateBody = String.format("{\n" +
                "  \"fN\": \"%s\",\n" +
                "  \"lN\": \"%s\",\n" +
                "  \"gender\": %d,\n" +
                "  \"ageBracket\": %d,\n" +
                "  \"phone\": \"%s\",\n" +
                "  \"email\": \"%s\",\n" +
                "  \"id\": \"%s\",\n" +
                "  \"pagetoken\": \"dummy-token\"\n" +
                "}",
                updatedFirstName, updatedLastName, updatedGender, updatedAgeBracket, updatedPhone, updatedEmail, uniqueId
        );
        // Send PUT request to update the user
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", uniqueId)
                .body(updateBody)
            .when()
                .put(TestConfig.USERS_ENDPOINT + "/{id}")
            .then()
                .statusCode(anyOf(is(200), is(204)))
                .extract().response();

        // If the response has a body, validate it; otherwise, skip validation
        String responseBody = response.asString();
        if (responseBody != null && !responseBody.trim().isEmpty() && responseBody.trim().startsWith("{")) {
            validateUserResponse(response, updatedFirstName, updatedLastName, updatedGender,
                                 updatedAgeBracket, updatedPhone, updatedEmail, null);
        } else {
            log.warn("Update user response body is empty or not JSON, skipping validation.");
        }
    }
    
    
    @AfterSuite(alwaysRun = true)
    public void cleanupTestData() {
        System.out.println("\n===== Cleaning up test data =====");
        int deletedCount = 0;
        for (String userId : new java.util.ArrayList<>(testUserIds)) {
            try {
                given()
                    .spec(requestSpec)
                    .pathParam("id", userId)
                .when()
                    .delete(TestConfig.USERS_ENDPOINT + "/{id}")
                .then()
                    .statusCode(200);
                deletedCount++;
                System.out.println("Deleted test user with ID: " + userId);
            } catch (Exception e) {
                System.err.println("Failed to delete test user with ID: " + userId + " - " + e.getMessage());
            }
        }
        System.out.println("Cleanup complete. Deleted " + deletedCount + " test users =====\n");
    }

    
    protected void addUserIdForCleanup(String userId) {
        if (userId != null && !userId.trim().isEmpty() && !testUserIds.contains(userId)) {
            testUserIds.add(userId);
        }
    }

    protected static RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            throw new IllegalStateException("RequestSpecification has not been initialized. Ensure setup() is called.");
        }
        return requestSpec;
    }
}
