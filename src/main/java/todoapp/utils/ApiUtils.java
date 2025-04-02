package todoapp.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import todoapp.config.TestConfig;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for API operations
 */
public class ApiUtils {
    private static final Logger logger = LoggerFactory.getLogger(ApiUtils.class);
    private static final TestConfig config = TestConfig.getInstance();

    private ApiUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Initialize RestAssured with base URL
     */
    public static void initRestAssured() {
        RestAssured.baseURI = config.getBaseUrl();
        RestAssured.filters(new AllureRestAssured());
    }

    /**
     * Get all TODO items
     *
     * @return Response object
     */
    public static Response getAllTodos() {
        logger.info("Getting all TODOs");
        return getBaseRequest()
            .get(ApiConstants.TODOS_ENDPOINT);
    }

    /**
     * Get TODOs with pagination
     *
     * @param offset number of items to skip
     * @param limit  maximum number of items to return
     * @return Response object
     */
    public static Response getTodosWithPagination(int offset, int limit) {
        logger.info("Getting TODOs with offset {} and limit {}", offset, limit);
        return getBaseRequest()
            .queryParam(ApiConstants.OFFSET, offset)
            .queryParam(ApiConstants.LIMIT, limit)
            .get(ApiConstants.TODOS_ENDPOINT);
    }

    /**
     * Get a TODO by ID
     *
     * @param id TODO ID
     * @return Response object
     */
    public static Response getTodoById(long id) {
        logger.info("Getting TODO with ID: {}", id);
        return getBaseRequest()
            .pathParam("id", id)
            .get(ApiConstants.TODO_BY_ID_ENDPOINT);
    }

    /**
     * Create a new TODO
     *
     * @param todo TodoDto object
     * @return Response object
     */
    public static Response createTodo(TodoDto todo) {
        logger.info("Creating a new TODO: {}", todo);
        return getBaseRequest()
            .body(todo)
            .post(ApiConstants.TODOS_ENDPOINT);
    }

    /**
     * Update a TODO
     *
     * @param id   TODO ID
     * @param todo Updated TodoDto object
     * @return Response object
     */
    public static Response updateTodo(long id, TodoDto todo) {
        logger.info("Updating TODO with ID {}: {}", id, todo);
        return getBaseRequest()
            .pathParam("id", id)
            .body(todo)
            .put(ApiConstants.TODO_BY_ID_ENDPOINT);
    }

    /**
     * Delete a TODO
     *
     * @param id TODO ID
     * @return Response object
     */
    public static Response deleteTodo(long id) {
        logger.info("Deleting TODO with ID: {}", id);
        return getBaseRequest()
            .pathParam("id", id)
            .header(ApiConstants.AUTHORIZATION, getBasicAuthHeader())
            .delete(ApiConstants.TODO_BY_ID_ENDPOINT);
    }

    /**
     * Create a TODO for testing and return the created object
     *
     * @param text TODO text
     * @return created TodoDto
     */
    public static TodoDto createTestTodo(String text) {
        TodoDto todoToCreate = TodoDto.createTodoWithText(text);
        Response response = createTodo(todoToCreate);

        if (response.getStatusCode() != ApiConstants.STATUS_CREATED &&
            response.getStatusCode() != ApiConstants.STATUS_OK) {
            logger.error("Failed to create test TODO: {}", response.asString());
            throw new RuntimeException("Failed to create test TODO. Status code: " + response.getStatusCode());
        }

        // Since the API returns an empty body, we need to get the created todo from the list
        Response getAllResponse = getAllTodos();
        List<TodoDto> todos = getTodosFromResponse(getAllResponse);

        TodoDto createdTodo = todos.stream()
            .filter(todo -> text.equals(todo.getText()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Created todo not found in todos list"));

        return createdTodo;
    }
    public static Response createTodoRaw(TodoDto todoData) {
        logger.info("Creating a new TODO with raw data: {}", todoData);
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(todoData)
            .post(ApiConstants.TODOS_ENDPOINT);
    }
    public static Response createTodoRaw(Map<String, Object> todoData) {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .body(todoData)
            .post(ApiConstants.TODOS_ENDPOINT);
    }

    public static Response updateTodoRaw(long id, Map<String, Object> updateData) {
        logger.info("Updating TODO with ID {} with raw data: {}", id, updateData);
        return getBaseRequest()
            .header(ApiConstants.AUTHORIZATION, getBasicAuthHeader()) // Add auth header
            .pathParam("id", id)
            .body(updateData)
            .put(ApiConstants.TODO_BY_ID_ENDPOINT);
    }
    public static Response updateTodoRaw(TodoDto updateData) {
        logger.info("Updating TODO with raw data: {}", updateData);

        // Check if ID is null before using it as a path parameter
        if (updateData.getId() == null) {
            // Use a placeholder ID or return a special response
            return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(updateData)
                .put(ApiConstants.TODOS_ENDPOINT + "/0");  // Use placeholder ID
        } else {
            return getBaseRequest()
                .pathParam("id", updateData.getId())
                .body(updateData)
                .put(ApiConstants.TODO_BY_ID_ENDPOINT);
        }
    }

    public static Response getTodosWithRawParams(Map<String, String> queryParams) {
        RequestSpecification request = getBaseRequest();
        for (Map.Entry<String, String> param : queryParams.entrySet()) {
            request = request.queryParam(param.getKey(), param.getValue());
        }
        return request.get(ApiConstants.TODOS_ENDPOINT);
    }

    public static Response deleteTodoWithoutAuth(long id) {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .pathParam("id", id)
            .delete(ApiConstants.TODO_BY_ID_ENDPOINT);
    }

    public static List<TodoDto> getTodosFromResponse(Response response) {
        return response.jsonPath().getList("", TodoDto.class);
    }

    /**
     * Clean up test data by deleting all TODOs
     */
    public static void cleanUpAllTodos() {
        List<TodoDto> todos = getTodosFromResponse(getAllTodos());
        for (TodoDto todo : todos) {
            deleteTodo(todo.getId());
        }
    }

    /**
     * Get base request specification with common settings
     *
     * @return RequestSpecification
     */
    public static RequestSpecification getBaseRequest() {
        return RestAssured.given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
    }

    /**
     * Generate Basic Auth header
     *
     * @return Basic Auth header value
     */
    public static String getBasicAuthHeader() {
        String auth = config.getAdminUsername() + ":" + config.getAdminPassword();
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }
}