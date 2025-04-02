package todoapp.api;

import org.testng.annotations.DataProvider;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.testng.Assert.*;

/**
 * Comprehensive test suite for PUT /todos/:id endpoint.
 * Validates todo update scenarios, including positive and negative cases.
 */
public class TodoUpdateTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoUpdateTest.class);
    private TodoDto existingTodo;

    /**
     * Prepares test environment before each test method.
     * Creates a todo for update testing and ensures a clean slate.
     */
    @BeforeMethod
    public void setUp() {
        logger.info("Preparing test environment for todo update tests");
        ApiUtils.initRestAssured();

        // Create a todo for update testing
        existingTodo = ApiUtils.createTestTodo("Todo for update testing");
        assertNotNull(existingTodo.getId(), "Failed to create test todo");
    }

    /**
     * Cleans up test environment after each test method.
     */
    @AfterMethod
    public void cleanUp() {
        logger.info("Cleaning up after todo update test");
        ApiUtils.cleanUpAllTodos();
    }

    /**
     * Verifies todo text can be successfully updated.
     */
    @Test(description = "Update todo text")
    public void testUpdateTodoText() {
        logger.info("Executing todo text update test");

        // Prepare update with new text
        String updatedText = "Updated todo text";
        TodoDto updateDto = TodoDto.builder()
            .id(existingTodo.getId())
            .text(updatedText)
            .completed(existingTodo.getCompleted())
            .build();

        // Perform update
        Response response = ApiUtils.updateTodo(existingTodo.getId(), updateDto);
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
            "Expected successful update");

        // Verify update
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        Optional<TodoDto> updatedTodoOpt = todos.stream()
            .filter(todo -> todo.getId().equals(existingTodo.getId()))
            .findFirst();

        assertTrue(updatedTodoOpt.isPresent(), "Updated todo should exist");
        assertEquals(updatedTodoOpt.get().getText(), updatedText,
            "Todo text should be updated");
    }

    /**
     * Verifies todo completion status can be successfully updated.
     */
    @Test(description = "Update todo completion status")
    public void testUpdateTodoCompletionStatus() {
        logger.info("Executing todo completion status update test");

        // Toggle completion status
        boolean newCompletionStatus = !existingTodo.getCompleted();
        TodoDto updateDto = TodoDto.builder()
            .id(existingTodo.getId())
            .text(existingTodo.getText())
            .completed(newCompletionStatus)
            .build();

        // Perform update
        Response response = ApiUtils.updateTodo(existingTodo.getId(), updateDto);
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_OK,
            "Expected successful update");

        // Verify update
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        Optional<TodoDto> updatedTodoOpt = todos.stream()
            .filter(todo -> todo.getId().equals(existingTodo.getId()))
            .findFirst();

        assertTrue(updatedTodoOpt.isPresent(), "Updated todo should exist");
        assertEquals(updatedTodoOpt.get().getCompleted(), newCompletionStatus,
            "Todo completion status should be updated");
    }

    /**
     * Verifies behavior when attempting to update a non-existent todo.
     */
    @Test(description = "Update non-existent todo")
    public void testUpdateNonExistentTodo() {
        logger.info("Executing non-existent todo update test");

        // Prepare update for non-existent todo
        long nonExistentId = Long.MAX_VALUE;
        TodoDto updateDto = TodoDto.builder()
            .id(nonExistentId)
            .text("Non-existent todo update")
            .completed(false)
            .build();

        // Verify the ID doesn't exist in the system
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);
        boolean idExists = todos.stream()
            .anyMatch(todo -> todo.getId().equals(nonExistentId));
        assertFalse(idExists, "Test ID should not exist before test execution");

        // Attempt update
        Response response = ApiUtils.updateTodo(nonExistentId, updateDto);

        // Validate response for non-existent todo - strictly expecting 404 Not Found
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_NOT_FOUND,
            "Expected 404 Not Found when updating non-existent todo");

        logger.info("API returned status code {} for non-existent ID update", response.getStatusCode());
    }

    /**
     * Verifies rejection of todo updates with missing required fields.
     */
    @Test(description = "Update todo with missing fields",
        dataProvider = "missingFieldsTestCases")
    public void testUpdateTodoWithMissingFields(String caseDescription, TodoDto updateData) {
        logger.info("Testing update {}", caseDescription);
        logger.info("Update data: {}", updateData);

        Response response = ApiUtils.updateTodoRaw(updateData);
        // Attempt the update


        // Log and verify response
        logger.info("Response Status Code: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.asString());

        assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
            "Expected 400 Bad Request for update " + caseDescription);

        // Verify todo remains unchanged
        verifyTodoUnchanged("Todo should remain unchanged after failed update " + caseDescription);
    }

    /**
     * Verifies API rejects updates with mismatched IDs.
     */
    @Test(description = "Reject updates with mismatched IDs")
    public void testUpdateWithMismatchedIds() {
        logger.info("Executing mismatched ID update test");

        // Create another todo with different ID
        TodoDto anotherTodo = ApiUtils.createTestTodo("Another Todo " + System.currentTimeMillis());

        // Prepare update DTO with ID that doesn't match the path parameter
        TodoDto updateDto = TodoDto.builder()
            .id(anotherTodo.getId())  // Different from path ID
            .text("Updated text")
            .completed(true)
            .build();

        // Attempt to update using original todo's ID in path
        Response response = ApiUtils.updateTodo(existingTodo.getId(), updateDto);

        // Verify API rejected the request
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
            "API should reject update when ID in body doesn't match ID in path");
    }

    /**
     * Verifies prevention of changing todo ID to an existing ID.
     */
    @Test(description = "Prevent changing todo ID to existing ID")
    public void testCannotUpdateToExistingId() {
        logger.info("Executing ID change prevention test");

        // Create another todo for the test
        TodoDto anotherTodo = ApiUtils.createTestTodo("Another Todo");
        assertNotNull(anotherTodo.getId(), "Failed to create second test todo");

        // Prepare update DTO attempting to use another todo's ID
        TodoDto updateDto = TodoDto.builder()
            .id(anotherTodo.getId())
            .text("Attempt to change ID")
            .completed(false)
            .build();

        // Attempt to update and verify rejection
        Response response = ApiUtils.updateTodo(existingTodo.getId(), updateDto);
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_BAD_REQUEST,
            "API should reject attempt to change todo ID to an existing ID");

        // Verify both todos remain unchanged
        verifyTodosUnchanged(existingTodo, anotherTodo);
    }

    /**
     * Verifies a todo remains unchanged after a failed update.
     *
     * @param message Context message for assertion
     */
    private void verifyTodoUnchanged(String message) {
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        Optional<TodoDto> todoOpt = todos.stream()
            .filter(todo -> todo.getId().equals(existingTodo.getId()))
            .findFirst();

        assertTrue(todoOpt.isPresent(), "Original todo should still exist");

        assertEquals(todoOpt.get(), existingTodo,
            message + " - todo should remain unchanged");
    }

    /**
     * Verifies multiple todos remain unchanged after a failed update.
     *
     * @param todosToCheck Todos to verify remain unmodified
     */
    private void verifyTodosUnchanged(TodoDto... todosToCheck) {
        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);

        for (TodoDto originalTodo : todosToCheck) {
            Optional<TodoDto> todoOpt = todos.stream()
                .filter(todo -> todo.getId().equals(originalTodo.getId()))
                .findFirst();

            assertTrue(todoOpt.isPresent(),
                "Todo with ID " + originalTodo.getId() + " should still exist");

            assertEquals(todoOpt.get(), originalTodo,
                "Todo should remain unchanged for ID " + originalTodo.getId());
        }
    }
    @DataProvider(name = "missingFieldsTestCases")
    public Object[][] missingFieldsTestCases() {
        return new Object[][] {
            {"without ID field", TodoDto.builder()
                .text("Attempted update without ID")
                .completed(true)
                .build()},
            {"without text field", TodoDto.builder()
                .id(existingTodo.getId())
                .completed(true)
                .build()},
            {"without completed field", TodoDto.builder()
                .id(existingTodo.getId())
                .text("Attempted update without completed field")
                .build()}
        };
    }
}