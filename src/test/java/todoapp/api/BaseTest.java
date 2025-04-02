package todoapp.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import todoapp.config.TestConfig;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.listeners.AllureReportListener;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * Base test class with common setup methods and helper methods for all tests
 */
@Listeners(AllureReportListener.class)
public class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static final TestConfig config = TestConfig.getInstance();

    @BeforeSuite
    public void suiteSetup() {
        logger.info("Setting up test suite");
        // Initialize REST Assured with base URL
        ApiUtils.initRestAssured();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        // Log configuration
        logger.info("Base URL: {}", config.getBaseUrl());
        logger.info("WebSocket URL: {}", config.getWsUrl());
    }

    @BeforeMethod
    public void baseSetup() {
        // Ensure clean slate for each test
        ApiUtils.cleanUpAllTodos();
    }

    protected TodoDto createAndVerifyTodo(TodoDto todoToCreate, boolean expectedCompletedStatus) {
        Response response = ApiUtils.createTodo(todoToCreate);

        // Verify status code
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
            "Expected status code 201 Created");

        // Verify todo exists with expected properties
        return verifyTodoExists(todoToCreate.getText(), expectedCompletedStatus);
    }

    /**
     * Verify a todo exists by searching for its text
     *
     * @param todoText Text to search for
     * @param expectedCompletedStatus Expected completed status, or null if not checking
     * @return The found todo
     */
    protected TodoDto verifyTodoExists(String todoText, Boolean expectedCompletedStatus) {
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        Optional<TodoDto> createdTodoOpt = todos.stream()
            .filter(todo -> todoText.equals(todo.getText()))
            .findFirst();

        assertTrue(createdTodoOpt.isPresent(),
            "Todo with text '" + todoText + "' should exist in the todos list");

        TodoDto createdTodo = createdTodoOpt.get();
        assertNotNull(createdTodo.getId(), "Created todo should have an ID");
        assertEquals(createdTodo.getText(), todoText, "Created todo text should match input");

        if (expectedCompletedStatus != null) {
            assertEquals(createdTodo.getCompleted(), expectedCompletedStatus,
                "Created todo completion status should be " + expectedCompletedStatus);
        }

        return createdTodo;
    }

    /**
     * Verify a todo exists by ID
     *
     * @param todoId ID to search for
     * @param expectedText Expected text
     * @param expectedCompleted Expected completed status
     * @return The found todo
     */
    protected TodoDto verifyTodoExistsById(long todoId, String expectedText, boolean expectedCompleted) {
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        Optional<TodoDto> createdTodoOpt = todos.stream()
            .filter(todo -> todoId == todo.getId())
            .findFirst();

        assertTrue(createdTodoOpt.isPresent(),
            "Todo with ID " + todoId + " should exist in the todos list");

        TodoDto createdTodo = createdTodoOpt.get();
        assertEquals(createdTodo.getText(), expectedText, "Created todo text should match expected");
        assertEquals(createdTodo.getCompleted(), expectedCompleted,
            "Created todo completion status should be " + expectedCompleted);

        return createdTodo;
    }

    /**
     * Verify multiple todos exist
     *
     * @param todoTexts Array of todo texts to verify
     */
    protected void verifyMultipleTodosExist(String[] todoTexts) {
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        int foundCount = 0;
        for (String text : todoTexts) {
            boolean found = todos.stream()
                .anyMatch(todo -> text.equals(todo.getText()));

            if (found) {
                foundCount++;
            }
        }

        assertEquals(foundCount, todoTexts.length,
            "Expected all " + todoTexts.length + " todos to be created");
    }

    /**
     * Verify a negative scenario with missing or invalid field
     *
     * @param todoData Map containing todo data
     * @param fieldName Name of the problematic field
     */



    protected void verifyNegativeScenario(TodoDto todoData, String fieldName) {
        String testName = Thread.currentThread().getStackTrace()[2].getMethodName();
        logger.info("[{}] Attempting to create/update with issue in field: {}", testName, fieldName);

        // Choose the appropriate API method
        Response response = ApiUtils.createTodoRaw(todoData);

        // Log response details for debugging
        logger.info("[{}] Response Status Code: {}", testName, response.getStatusCode());
        logger.info("[{}] Response Body: {}", testName, response.asString());

        // Check status code is 400 Bad Request
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
            "Expected 400 Bad Request for operation with issue in field: " + fieldName);

        // If todoText provided, verify it wasn't created/updated
        if (todoData.getText() != null) {
            Response getAllResponse = ApiUtils.getAllTodos();
            List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);
            boolean todoExists = todos.stream()
                .anyMatch(todo -> todoData.getText().equals(todo.getText()));
            assertFalse(todoExists, "No todo should be created/updated with text: " + todoData.getText());
        }
    }
    /**
     * Validate error response with more flexible checking
     *
     * @param response The API response
     * @param fieldName The field expected to have an issue
     */
    private void validateErrorResponse(Response response, String fieldName) {
        // Simply verify the status code indicates an error
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
            "Expected 400 Bad Request for issue with field: " + fieldName);

        // Log the response body for debugging purposes
        logger.debug("Error response: {}", response.asString());
    }

    /**
     * Generate a unique ID for test data
     * @return A unique ID based on timestamp
     */
    protected long generateUniqueId() {
        return System.currentTimeMillis();
    }

    /**
     * Generate a test-specific string with unique identifier
     * @param prefix Text prefix
     * @return String with unique identifier
     */
    protected String generateUniqueText(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

}