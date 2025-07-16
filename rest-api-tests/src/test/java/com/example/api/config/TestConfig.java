package com.example.api.config;

public class TestConfig {
    public static final String BASE_URI = "https://9tupht8p68.execute-api.us-east-1.amazonaws.com";
    public static final boolean ENABLE_REQUEST_LOGGING = true;
    public static final String USERS_ENDPOINT = "/api/users";
    public static final String GROUPS_ENDPOINT = "/api/groups";
    public static final String MESSAGES_ENDPOINT = "/api/messages";
    
    // Test Data
    public static class TestUsers {
        public static final String ADMIN_USER_ID = "a097c285-72b2-4b77-b612-e44a625acc0d";
        public static final String MEMBER_USER_ID = "a097c285-72b2-4b77-b612-e44a757acc0d";
        public static final String NON_MEMBER_EMAIL = "fn3.ln3@example.com";
        public static final String MEMBER_EMAIL = "fn2.ln2@success.com";
        public static final String ADMIN_EMAIL = "fn6.ln6@success.com";
    }
    
    public static class TestGroups {
        public static final String TEST_GROUP_ID = "test-group-id-123";
    }
}
