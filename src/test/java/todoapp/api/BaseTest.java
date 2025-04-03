package todoapp.api;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;
import todoapp.config.TestConfig;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.listeners.AllureReportListener;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.testng.Assert.*;


@Listeners(AllureReportListener.class)
public class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static final TestConfig config = TestConfig.getInstance();

    @BeforeSuite(alwaysRun = true)
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

    /**
     * Setup before each test method to ensure a clean environment.
     * Cleans up all todos to provide isolation between tests.
     */
    @BeforeMethod(alwaysRun = true)
    public void baseSetup() {
        String methodName = getCallingMethodName();
        logger.info("Setting up test environment for: {}", methodName);

        // Clean slate for test isolation
        try {
            ApiUtils.cleanUpAllTodos();
            logger.info("Successfully cleaned up all todos before test");
        } catch (Exception e) {
            logger.warn("Failed to clean up todos before test: {}", e.getMessage());
        }
    }

    /**
     * Cleanup after each test method.
     * Ensures test data is removed even if test fails.
     */
    @AfterMethod(alwaysRun = true)
    public void baseTearDown() {
        String methodName = getCallingMethodName();
        logger.info("Cleaning up after test: {}", methodName);

        try {
            ApiUtils.cleanUpAllTodos();
            logger.info("Successfully cleaned up all todos after test");
        } catch (Exception e) {
            logger.warn("Failed to clean up todos after test: {}", e.getMessage());
        }
    }

    protected TodoDto createAndVerifyTodo(TodoDto todoToCreate, boolean expectedCompletedStatus) {
        logger.info("Creating todo: {}", todoToCreate);
        Response response = ApiUtils.createTodo(todoToCreate);

        // Verify status code
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
            "Expected status code 201 Created for todo creation");

        // Verify todo exists with expected properties
        return verifyTodoExists(todoToCreate.getText(), expectedCompletedStatus);
    }


    protected TodoDto verifyTodoExists(String todoText, Boolean expectedCompletedStatus) {
        logger.info("Verifying todo exists with text: '{}'", todoText);
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

        logger.info("Successfully verified todo exists: {}", createdTodo);
        return createdTodo;
    }

    /**
     * Verifies a todo exists by ID and validates its properties.
     *
     * @param todoId ID to search for
     * @param expectedText Expected text
     * @param expectedCompleted Expected completed status
     * @return The verified TodoDto
     */
    protected TodoDto verifyTodoExistsById(long todoId, String expectedText, boolean expectedCompleted) {
        logger.info("Verifying todo exists with ID: {}", todoId);
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

        logger.info("Successfully verified todo exists by ID: {}", createdTodo);
        return createdTodo;
    }

    /**
     * Verifies multiple todos exist with the given texts.
     *
     * @param todoTexts Array of todo texts to verify
     */
    protected void verifyMultipleTodosExist(String[] todoTexts) {
        logger.info("Verifying {} todos exist", todoTexts.length);
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        int foundCount = 0;
        for (String text : todoTexts) {
            boolean found = todos.stream()
                .anyMatch(todo -> text.equals(todo.getText()));

            if (found) {
                foundCount++;
                logger.debug("Found todo with text: '{}'", text);
            } else {
                logger.warn("Could not find todo with text: '{}'", text);
            }
        }

        assertEquals(foundCount, todoTexts.length,
            "Expected all " + todoTexts.length + " todos to be created, but found " + foundCount);

        logger.info("Successfully verified all {} todos exist", todoTexts.length);
    }

    /**
     * Verifies a negative scenario with missing or invalid field.
     * Used for validating error cases in API tests.
     *
     * @param todoData DTO containing todo data with issues
     * @param fieldName Name of the problematic field
     */
    protected void verifyNegativeScenario(TodoDto todoData, String fieldName) {
        String methodName = getCallingMethodName();
        logger.info("[{}] Testing negative scenario with issue in field: {}", methodName, fieldName);

        // Perform the operation that should fail
        Response response = ApiUtils.createTodoRaw(todoData);

        // Log response details for debugging
        logger.info("[{}] Response Status Code: {}", methodName, response.getStatusCode());
        logger.debug("[{}] Response Body: {}", methodName, response.asString());

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

        logger.info("[{}] Successfully verified negative scenario for field: {}", methodName, fieldName);
    }

    /**
     * Generates a unique ID for test data based on current timestamp.
     * Useful for creating unique objects in tests.
     *
     * @return A unique ID based on timestamp
     */
    protected long generateUniqueId() {
        return System.currentTimeMillis();
    }

    /**
     * Generates a test-specific string with unique identifier.
     * Useful for creating unique text values in tests.
     *
     * @param prefix Text prefix
     * @return String with unique identifier
     */
    protected String generateUniqueText(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * Gets the calling method name for better logging.
     *
     * @return Name of the method that called the current method
     */
    private String getCallingMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Index 2 would be the method that called this method
        return stackTrace.length > 3 ? stackTrace[3].getMethodName() : "unknown";
    }
}