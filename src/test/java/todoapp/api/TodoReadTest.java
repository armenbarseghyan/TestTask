package todoapp.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.BeforeMethod;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;
import static todoapp.constants.ApiConstants.APPLICATION_JSON;
import static todoapp.constants.ApiConstants.CONTENT_TYPE;

/**
 * Comprehensive test suite for GET /todos endpoint functionality.
 * Verifies retrieval of todo items with various scenarios.
 */
@Epic("Tech-task")
@Feature("Todo Read")
public class TodoReadTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoReadTest.class);
    private TodoDto testTodo1;
    private TodoDto testTodo2;

    /**
     * Prepares test environment by initializing REST Assured and creating test todos.
     */
    @BeforeMethod
    public void setUp() {
        // Create test todos for verification
        testTodo1 = ApiUtils.createTestTodo("First test todo for read operations");
        testTodo2 = ApiUtils.createTestTodo("Second test todo for read operations");

        assertNotNull(testTodo1.getId(), "First test todo creation failed");
        assertNotNull(testTodo2.getId(), "Second test todo creation failed");
    }

    /**
     * Verifies retrieval of all todos with precise content validation.
     */
    @Test(description = "Verify complete todo list retrieval")
    public void testGetAllTodos() {
        logger.info("Executing all todos retrieval test");

        Response response = ApiUtils.getAllTodos();

        // Validate response structure and content
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK, "Unexpected response status");
        assertEquals(response.getContentType(), APPLICATION_JSON, "Incorrect content type");

        List<TodoDto> todos = ApiUtils.getTodosFromResponse(response);

        // Strict todos list validation
        assertNotNull(todos, "Todo list should not be null");
        assertEquals(todos.size(), 2, "Incorrect number of todos");

        // Verify specific todos exist
        boolean firstTodoFound = todos.stream()
            .anyMatch(todo ->
                todo.getId().equals(testTodo1.getId()) &&
                    todo.getText().equals(testTodo1.getText()) &&
                    todo.getCompleted().equals(testTodo1.getCompleted())
            );

        boolean secondTodoFound = todos.stream()
            .anyMatch(todo ->
                todo.getId().equals(testTodo2.getId()) &&
                    todo.getText().equals(testTodo2.getText()) &&
                    todo.getCompleted().equals(testTodo2.getCompleted())
            );

        assertTrue(firstTodoFound, "First test todo not found in list");
        assertTrue(secondTodoFound, "Second test todo not found in list");
    }

    /**
     * Verifies retrieval of a single todo by its exact ID.
     */
    @Test(description = "Retrieve todo by specific ID")
    public void testGetTodoById() {
        logger.info("Executing single todo retrieval test");

        Response response = ApiUtils.getTodoById(testTodo1.getId());

        // Validate response and retrieved todo
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK, "Unexpected response status");

        TodoDto retrievedTodo = response.as(TodoDto.class);

        assertNotNull(retrievedTodo, "Retrieved todo should not be null");
        assertEquals(retrievedTodo.getId(), testTodo1.getId(), "Mismatched todo ID");
        assertEquals(retrievedTodo.getText(), testTodo1.getText(), "Mismatched todo text");
        assertEquals(retrievedTodo.getCompleted(), testTodo1.getCompleted(), "Mismatched completion status");
    }

    /**
     * Attempts to retrieve a non-existent todo to verify error handling.
     */
    @Test(description = "Retrieve non-existent todo")
    public void testGetNonExistentTodo() {
        logger.info("Executing non-existent todo retrieval test");

        long nonExistentId = Long.MAX_VALUE;
        Response response = ApiUtils.getTodoById(nonExistentId);

        // Validate error response for non-existent todo
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_NOT_FOUND, "Unexpected response status");
        assertNotNull(response.asString(), "Error response body should not be null");
    }

    /**
     * Verifies response headers for todo retrieval operations.
     */
    @Test(description = "Validate response headers")
    public void testResponseHeaders() {
        logger.info("Executing response headers validation test");

        Response response = ApiUtils.getAllTodos();

        // Check critical response headers
        assertNotNull(response.getHeader("Date"), "Date header missing");
        assertNotNull(response.getHeader(CONTENT_TYPE), "Content-Type header missing");
        assertEquals(response.getContentType(), APPLICATION_JSON, "Incorrect content type");
    }

    /**
     * Verifies behavior when no todos exist in the system.
     */
    @Test(description = "Retrieve todos from empty list")
    public void testGetAllWhenEmpty() {
        logger.info("Executing empty todos list retrieval test");
        ApiUtils.cleanUpAllTodos();
        Response response = ApiUtils.getAllTodos();

        // Validate empty list scenario
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK, "Unexpected response status");

        List<TodoDto> todos = ApiUtils.getTodosFromResponse(response);
        assertEquals(todos.size(), 0, "Todo list should be empty");
    }

    /**
     * Cleans up test environment after test suite completion.
     */
    @AfterClass
    public void tearDown() {
        logger.info("Cleaning up after GET endpoint tests");
        ApiUtils.cleanUpAllTodos();
    }
}