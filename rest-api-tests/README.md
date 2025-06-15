# REST API Test Automation Framework

This is a REST API test automation framework built with Java, Rest Assured, and TestNG. The framework is designed to test CRUD operations and various API endpoints.

## Prerequisites

- Java JDK 11 or higher
- Maven 3.6.0 or higher
- Internet connection (to access the test API)

## Project Structure

```
rest-api-tests/
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── api/
│                       ├── base/           # Base test classes
│                       ├── config/         # Configuration files
│                       ├── listeners/      # Test listeners
│                       └── tests/          # Test classes
└── pom.xml                                # Maven configuration
```

## Setup Instructions

1. **Clone the repository** (if applicable) or extract the project to your local machine.

2. **Navigate to the project directory**:
   ```bash
   cd rest-api-tests
   ```

3. **Run the tests**:
   ```bash
   mvn clean test
   ```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Suite
```bash
mvn test -DsuiteXmlFile=testng.xml
```

### Run Specific Test Class
```bash
mvn test -Dtest=UserAPITest
```

### Run Tests with Custom Base URL
```bash
mvn test -Dbase.url=https://your-api-url.com
```

## Test Cases

### User API Tests
- Create a new user
- Create user with missing required fields (negative test)
- Get user by ID
- Get non-existent user (negative test)
- Update user information
- Delete user

### Group API Tests
- Create a new group
- Create group with missing required fields (negative test)
- Get group by ID
- Get non-existent group (negative test)
- Add member to group
- Add member without admin rights (negative test)
- Delete group

### Message API Tests
- Post message to group
- Post message to invalid group (negative test)
- Get messages for group
- Get message by ID
- Get non-existent message (negative test)
- Update message
- Delete message

## Test Reports

TestNG generates HTML reports in the `target/surefire-reports` directory after test execution.

## Configuration

Update the following in `src/test/java/com/example/api/config/TestConfig.java` as needed:
- `BASE_URI`: The base URL of the API under test
- Test data constants for users, groups, etc.

## Dependencies

- Rest Assured 5.3.0 - For REST API testing
- TestNG 7.7.1 - Test framework
- Jackson Databind 2.14.2 - For JSON processing
- Lombok 1.18.26 - For reducing boilerplate code
- SLF4J 2.0.5 - For logging

## Best Practices

1. **Test Isolation**: Each test should be independent and not rely on the state from other tests.
2. **Data Cleanup**: Tests should clean up any test data they create.
3. **Assertions**: Include meaningful assertions to verify both positive and negative scenarios.
4. **Error Handling**: Implement proper error handling and logging.
5. **Configuration**: Keep test data and configuration separate from test logic.

## Troubleshooting

- **Connection Issues**: Ensure the API is accessible from your network.
- **Test Failures**: Check the test reports for detailed error messages.
- **Dependency Issues**: Run `mvn clean install` to resolve dependency problems.
