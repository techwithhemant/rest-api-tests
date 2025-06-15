package com.example.api.tests;

import com.example.api.base.BaseTest;
import com.example.api.config.TestConfig;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAPITest extends BaseTest {

    private static final Logger log = LoggerFactory.getLogger(UserAPITest.class);
    // List to store IDs of test users created during test execution
    private static final List<String> testUserIds = new ArrayList<>();
    private void validateUserResponse(Response response, String expectedFirstName, String expectedLastName, 
            int expectedGender, int expectedAgeBracket, String expectedPhone, String expectedEmail, 
            Object expectedGroups) {
        
        JsonPath jsonPath = response.jsonPath();
        
        // Validate basic fields
        assertThat("First name should match", jsonPath.getString("fN"), equalTo(expectedFirstName));
        assertThat("Last name should match", jsonPath.getString("lN"), equalTo(expectedLastName));
        assertThat("Gender should match", jsonPath.getInt("gender"), equalTo(expectedGender));
        assertThat("Age bracket should match", jsonPath.getInt("ageBracket"), equalTo(expectedAgeBracket));
        assertThat("Phone should match", jsonPath.getString("phone"), equalTo(expectedPhone));
        assertThat("Email should match", jsonPath.getString("email"), equalTo(expectedEmail));
        
        // Validate groups (which can be null)
        assertThat("Groups should match", jsonPath.get("groups"), equalTo(expectedGroups));
        
        // Validate ID is present and is a valid UUID
        String userId = jsonPath.getString("id");
        assertThat("User ID should not be null", userId, notNullValue());
        String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
        assertThat("User ID should be a valid UUID", userId.matches(uuidPattern));
        
        // Validate pageToken (which is null in the response examples)
        assertThat("Page token should match", jsonPath.get("pageToken"), equalTo(null));
    }

    @Test(description = "Verify that a new user can be created successfully with all required fields")
    public void testCreateFirstUserSuccessfully() {
        String requestBody = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1212121556\",\n" +
                "  \"email\": \"fn6.ln6@success.com\",\n" +
                "  \"id\": \"" + TestConfig.TestUsers.ADMIN_USER_ID + "\"\n" +
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
        validateUserResponse(response, "FN1", "LN1", 1, 30, "1212121556", "fn6.ln6@success.com", null);
    }
    
    @Test(description = "Verify that a second user can be created with different details")
    public void testCreateSecondUserSuccessfully() {
        String requestBody = "{\n" +
                "  \"fN\": \"FN2\",\n" +
                "  \"lN\": \"LN2\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1626364656\",\n" +
                "  \"email\": \"fn2.ln2@success.com\",\n" +
                "  \"id\": \"" + TestConfig.TestUsers.MEMBER_USER_ID + "\"\n" +
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
        validateUserResponse(response, "FN2", "LN2", 1, 30, "1626364656", "fn2.ln2@success.com", null);
    }
    
    @Test(description = "Verify the user with the same ID can't be created")
    public void testCreateUserWithSameId() {
        String duplicateIdRequest = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1111111111\",\n" +
                "  \"email\": \"another.email@success.com\",\n" +
                "  \"id\": \"" + TestConfig.TestUsers.ADMIN_USER_ID + "\"\n" +
                "}";
    
        given()
            .spec(requestSpec)
            .body(duplicateIdRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("already exists"));
    }
    
    @Test(description = "Verify that creating a user with invalid phone number fails")
    public void testCreateUserWithInvalidPhone() {
        String invalidPhoneRequest = "{\n" +
                "  \"fN\": \"FN1\",\n" +
                "  \"lN\": \"LN1\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"invalid-phone\",\n" +
                "  \"email\": \"valid.email@success.com\",\n" +
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000001\"\n" +
                "}";
    
        given()
            .spec(requestSpec)
            .body(invalidPhoneRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("invalid phone"));
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
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000002\"\n" +
                "}";
    
        given()
            .spec(requestSpec)
            .body(invalidEmailRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("invalid email"));
    }
    
    @Test(description = "Verify user with the same email address can't be created")
    public void testCreateUserWithSameEmail() {
        String duplicateEmailRequest = "{\n" +
                "  \"fN\": \"FN_NEW\",\n" +
                "  \"lN\": \"LN_NEW\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"9998887776\",\n" +
                "  \"email\": \"" + TestConfig.TestUsers.ADMIN_EMAIL + "\",\n" +
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000003\"\n" +
                "}";
    
        given()
            .spec(requestSpec)
            .body(duplicateEmailRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("email already exists"));
    }
    
    @Test(description = "Verify user with the same phone number can't be created")
    public void testCreateUserWithSamePhone() {
        String duplicatePhoneRequest = "{\n" +
                "  \"fN\": \"FN_NEW\",\n" +
                "  \"lN\": \"LN_NEW\",\n" +
                "  \"gender\": 1,\n" +
                "  \"ageBracket\": 30,\n" +
                "  \"phone\": \"1212121556\",\n" +
                "  \"email\": \"new.email@success.com\",\n" +
                "  \"id\": \"a0a0a0a0-0000-0000-0000-000000000004\"\n" +
                "}";
    
        given()
            .spec(requestSpec)
            .body(duplicatePhoneRequest)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("phone already exists"));
    }
    

@Test(description = "Verify that creating a user with missing required fields fails")
    public void testCreateUserWithMissingFields() {
        String invalidRequestBody = "{\n" +
                "  \"fN\": \"Test\",\n" +
                "  \"gender\": 1\n" + // Missing last name, email, and other required fields
                "}";

        given()
            .spec(requestSpec)
            .body(invalidRequestBody)
        .when()
            .post(TestConfig.USERS_ENDPOINT)
        .then()
            .statusCode(400)
            .body("message", not(emptyOrNullString()))
            .body("message", containsStringIgnoringCase("missing required fields"));
    }

    @Test(description = "Verify that a user can be retrieved by ID")
    public void testGetUserById() {
        Response response = given()
            .spec(requestSpec)
            .pathParam("id", TestConfig.TestUsers.ADMIN_USER_ID)
        .when()
            .get(TestConfig.USERS_ENDPOINT + "/{id}")
        .then()
            .statusCode(200)
            .extract().response();
            
        // Verify the ID matches the expected ID
        assertThat(response.jsonPath().getString("id"), equalTo(TestConfig.TestUsers.ADMIN_USER_ID));
    
    }

    @Test(description = "Verify that a non-existent user returns 404")
    public void testGetNonExistentUser() {
        String nonExistentId = "non-existent-id-123";
        
        given()
            .spec(requestSpec)
            .pathParam("id", nonExistentId)
        .when()
            .get(TestConfig.USERS_ENDPOINT + "/{id}")
        .then()
            .statusCode(404);
    }

    @Test(description = "Verify that a user can be updated successfully")
    public void testUpdateUser() {
        // Updated user details
        String updatedFirstName = "Hemant";
        String updatedLastName = "Srivastava";
        int updatedGender = 1;
        int updatedAgeBracket = 35;
        String updatedPhone = "9876543210";
        String updatedEmail = "hemant.srivastava@success.com";
    
        // Prepare update request body with proper String.format() placeholders
        String updateBody = String.format("{\n" +
                "  \"fN\": \"%s\",\n" +
                "  \"lN\": \"%s\",\n" +
                "  \"gender\": %d,\n" +
                "  \"ageBracket\": %d,\n" +
                "  \"phone\": \"%s\",\n" +
                "  \"email\": \"%s\"\n" +
                "}",
                updatedFirstName, updatedLastName, updatedGender, updatedAgeBracket, updatedPhone, updatedEmail
        );
    
        // Send PUT request to update the user
        Response response = given()
                .spec(requestSpec)
                .pathParam("id", TestConfig.TestUsers.MEMBER_USER_ID)
                .body(updateBody)
            .when()
                .put(TestConfig.USERS_ENDPOINT + "/{id}")
            .then()
                .statusCode(200)
                .extract().response();
    
        // Validate the response with updated values
        validateUserResponse(response, updatedFirstName, updatedLastName, updatedGender,
                             updatedAgeBracket, updatedPhone, updatedEmail, null);
    }
    
    
    @AfterSuite(alwaysRun = true)
    public void cleanupTestData() {
        System.out.println("\n===== Cleaning up test data =====");
        int deletedCount = 0;

        // Delete all test users created during test execution
        for (String userId : new ArrayList<>(testUserIds)) { // Use a copy to avoid ConcurrentModificationException
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
    

    protected static void addUserIdForCleanup(String userId) {
        if (userId != null && !userId.trim().isEmpty() && !testUserIds.contains(userId)) {
            testUserIds.add(userId);
        }
    }
}
