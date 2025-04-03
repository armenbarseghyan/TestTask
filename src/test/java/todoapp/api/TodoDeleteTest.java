package todoapp.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Comprehensive test suite for DELETE /todos/:id endpoint.
 * Covers various todo deletion scenarios and security validations.
 */
@Epic("Tech-task")
@Feature("Todo Delete")
public class TodoDeleteTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoDeleteTest.class);
    private TodoDto testTodo;


    /**
     * Prepares a test todo for each test method to ensure isolation.
     */
    @BeforeMethod
    public void prepareTestTodo() {
        ApiUtils.cleanUpAllTodos();
        logger.info("Preparing test todo for deletion test");
        String uniqueText = "Todo for deletion test " + System.currentTimeMillis();
        testTodo = ApiUtils.createTestTodo(uniqueText);

        assertNotNull(testTodo.getId(), "Failed to create test todo");

        // Verify todo creation
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);
        boolean todoExists = todos.stream()
            .anyMatch(todo -> todo.getId().equals(testTodo.getId()));

        assertTrue(todoExists, "Test todo should exist before test execution");

    }

    /**
     * Verifies successful todo deletion with proper authorization.
     */
    @Test(description = "Delete todo with valid authorization")
    public void testDeleteTodoWithAuth() {
        logger.info("Executing authorized todo deletion test");

        // Perform deletion
        Response response = ApiUtils.deleteTodo(testTodo.getId());
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_NO_CONTENT,
            "Expected 204 No Content for successful delete");

        // Verify deletion
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> remainingTodos = ApiUtils.getTodosFromResponse(getAllResponse);
        boolean todoStillExists = remainingTodos.stream()
            .anyMatch(todo -> todo.getId().equals(testTodo.getId()));

        assertFalse(todoStillExists, "Todo should be deleted and not found in the list");
    }

    /**
     * Verifies deletion is prevented without proper authorization.
     * Tests multiple authentication scenarios: no auth, invalid credentials.
     */
    @Test(description = "Prevent todo deletion without proper authorization")
    public void testDeleteTodoWithoutProperAuth() {
        logger.info("Executing unauthorized todo deletion test scenarios");

        // Scenario 1: No authentication
        Response responseNoAuth = ApiUtils.deleteTodoWithoutAuth(testTodo.getId());
        assertEquals(responseNoAuth.getStatusCode(), ApiConstants.STATUS_UNAUTHORIZED,
            "Expected 401 Unauthorized for delete without any authorization");

        // Scenario 2: Invalid credentials
        Response responseWrongAuth = ApiUtils.getBaseRequest()
            .pathParam("id", testTodo.getId())
            .header(ApiConstants.AUTHORIZATION, "Basic aW52YWxpZDppbnZhbGlk") // invalid:invalid
            .delete(ApiConstants.TODO_BY_ID_ENDPOINT);
        assertEquals(responseWrongAuth.getStatusCode(), ApiConstants.STATUS_UNAUTHORIZED,
            "Expected 401 Unauthorized for delete with invalid credentials");

        // Confirm todo remains unchanged after both attempts
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> remainingTodos = ApiUtils.getTodosFromResponse(getAllResponse);
        boolean todoStillExists = remainingTodos.stream()
            .anyMatch(todo -> todo.getId().equals(testTodo.getId()));

        assertTrue(todoStillExists, "Todo should not be deleted without proper authorization");
    }

    /**
     * Verifies behavior when attempting to delete a non-existent todo.
     */
    @Test(description = "Delete non-existent todo")
    public void testDeleteNonExistentTodo() {
        logger.info("Executing non-existent todo deletion test");

        // Generate a non-existent ID
        long nonExistentId = 9999L + System.currentTimeMillis() % 1000;

        // Verify the ID doesn't exist
        Response getAllBeforeResponse = ApiUtils.getAllTodos();
        List<TodoDto> todosBefore = ApiUtils.getTodosFromResponse(getAllBeforeResponse);
        boolean idExists = todosBefore.stream()
            .anyMatch(todo -> todo.getId().equals(nonExistentId));
        assertFalse(idExists, "Test ID should not exist before test execution");

        // Attempt to delete non-existent todo
        Response response = ApiUtils.deleteTodo(nonExistentId);

        // Validate response for non-existent todo
        int statusCode = response.getStatusCode();
        assertTrue(
            statusCode == ApiConstants.STATUS_NOT_FOUND ||
                statusCode == ApiConstants.STATUS_NO_CONTENT,
            "Expected 404 Not Found or 204 No Content for non-existent ID"
        );

        logger.info("API returned status code {} for non-existent ID", statusCode);
    }

    /**
     * Verifies todo deletion and subsequent inaccessibility.
     */
    @Test(description = "Delete todo and verify removal")
    public void testDeleteTodoAndVerifyInaccessible() {
        logger.info("Executing todo deletion and verification test");

        // Record initial todo count
        Response getAllBeforeResponse = ApiUtils.getAllTodos();
        List<TodoDto> todosBefore = ApiUtils.getTodosFromResponse(getAllBeforeResponse);
        int todosCountBefore = todosBefore.size();

        // Delete the todo
        Response deleteResponse = ApiUtils.deleteTodo(testTodo.getId());
        assertEquals(deleteResponse.getStatusCode(), ApiConstants.STATUS_NO_CONTENT,
            "Expected 204 No Content for deletion");

        // Verify todo list changes
        Response getAllAfterResponse = ApiUtils.getAllTodos();
        List<TodoDto> todosAfter = ApiUtils.getTodosFromResponse(getAllAfterResponse);

        // Validate todo count reduction
        assertEquals(todosAfter.size(), todosCountBefore - 1,
            "Todo count should decrease by 1 after deletion");

        // Confirm todo is no longer accessible
        boolean todoStillExists = todosAfter.stream()
            .anyMatch(todo -> todo.getId().equals(testTodo.getId()));

        assertFalse(todoStillExists, "Deleted todo should not be accessible");
    }

    /**
     * Verifies deletion prevention with negative ID.
     */
    @Test(description = "Prevent deletion with negative ID")
    public void testDeleteTodoWithNegativeId() {
        logger.info("Executing deletion with negative ID test");

        // Generate a negative ID
        long negativeId = -Math.abs(System.currentTimeMillis());

        // Attempt deletion with negative ID
        Response response = ApiUtils.getBaseRequest()
            .pathParam("id", negativeId)
            .header(ApiConstants.AUTHORIZATION, ApiUtils.getBasicAuthHeader())
            .delete(ApiConstants.TODO_BY_ID_ENDPOINT);

        // Validate the specific error response
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_NOT_FOUND,
            "Expected 404 Not Found for negative ID");

        logger.info("API returned status code {} for negative ID", response.getStatusCode());
    }
    @Test(description = "Delete multiple todos")
    public void testDeleteMultipleTodos() {
        logger.info("Executing multiple todo deletion test");

        // Create 5 todos for deletion
        TodoDto[] todos = new TodoDto[5];
        for (int i = 0; i < 5; i++) {
            String uniqueText = "Multiple deletion test " + i;
            todos[i] = ApiUtils.createTestTodo(uniqueText);
            assertNotNull(todos[i].getId(), "Todo " + i + " should be created");
        }

        // Record initial count
        Response getAllBeforeResponse = ApiUtils.getAllTodos();
        List<TodoDto> todosBefore = ApiUtils.getTodosFromResponse(getAllBeforeResponse);
        int countBefore = todosBefore.size();

        // Delete all test todos
        for (TodoDto todo : todos) {
            Response response = ApiUtils.deleteTodo(todo.getId());
            assertEquals(response.getStatusCode(), ApiConstants.STATUS_NO_CONTENT,
                "Expected 204 No Content for deletion");
        }

        // Verify todos are gone
        Response getAllAfterResponse = ApiUtils.getAllTodos();
        List<TodoDto> todosAfter = ApiUtils.getTodosFromResponse(getAllAfterResponse);

        assertEquals(todosAfter.size(), countBefore - 5,
            "Todo count should decrease by 5 after deletions");
    }
    /**
     * Verifies behavior when deleting a non-existent todo ID.
     */

    @AfterClass
    public void tearDown() {
        logger.info("Cleaning up after todo deletion tests");
        ApiUtils.cleanUpAllTodos();
    }
}