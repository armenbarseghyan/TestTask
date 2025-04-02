package todoapp.constants;

/**
 * Centralized constants for API testing and configuration.
 * Provides endpoint paths, HTTP status codes, and default test values.
 */
public final class ApiConstants {
    // Prevent instantiation
    private ApiConstants() {}

    // API Endpoint Definitions
    public static final String TODOS_ENDPOINT = "/todos";
    public static final String TODO_BY_ID_ENDPOINT = "/todos/{id}";

    // HTTP Header Constants
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String AUTHORIZATION = "Authorization";
    public static final String APPLICATION_JSON = "application/json";

    // Query Parameter Names
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";

    // HTTP Status Codes
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_NOT_FOUND = 404;


}