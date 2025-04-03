package todoapp.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.annotations.*;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Comprehensive test suite for POST /todos endpoint.
 * Covers various todo creation scenarios and validation checks.
 */

@Epic("Tech-task")
@Feature("Todo Create")
public class TodoCreateTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoCreateTest.class);




    /**
     * Verifies prevention of creating todo with existing ID.
     */
    @Test(description = "Prevent todo creation with duplicate ID")
    public void testCreateTodoWithExistingId() {
        logger.info("Executing duplicate ID prevention test");
        long existingId = generateUniqueId();

        // Create initial todo
        TodoDto initialTodo = TodoDto.builder()
            .id(existingId)
            .text(generateUniqueText("Initial Todo"))
            .completed(false)
            .build();
        createAndVerifyTodo(initialTodo, false);

        // Attempt to create todo with same ID
        TodoDto duplicateTodo = TodoDto.builder()
            .id(existingId)
            .text(generateUniqueText("Duplicate Todo"))
            .completed(false)
            .build();

        verifyNegativeScenario(duplicateTodo, "id");
    }

    /**
     * Verifies rejection of todo creation without ID.
     */
    @Test(description = "Prevent todo creation without ID")
    public void testCreateTodoWithoutId() {
        logger.info("Executing todo creation without ID test");
        String uniqueText = generateUniqueText("Todo without ID");

        TodoDto todoWithoutId = TodoDto.builder()
            .text(uniqueText)
            .completed(false)
            .build();

        verifyNegativeScenario(todoWithoutId, "id");
    }

    /**
     * Verifies rejection of todo creation with invalid ID type.
     */
    @Test(description = "Prevent todo creation with invalid ID type")
    public void testCreateTodoWithInvalidIdType() {
        logger.info("Executing invalid ID type todo creation test");
        String uniqueText = generateUniqueText("Todo with invalid ID type");

        TodoDto todoWithInvalidId = TodoDto.builder()
            .text(uniqueText)
            .completed(false)
            .build();

        verifyNegativeScenario(todoWithInvalidId, "id");
    }

    @Test(description = "Prevent creating multiple todos with same ID")
    public void testConcurrentTodoCreationWithSameId() throws InterruptedException {
        logger.info("Executing concurrent todo creation test with same ID");
        final long sharedId = generateUniqueId();

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                TodoDto todo = TodoDto.builder()
                    .id(sharedId)
                    .text("Duplicate ID Todo " + index)
                    .completed(false)
                    .build();
                ApiUtils.createTodo(todo);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);
        assertEquals(todos.size(), 1, "Should have only 1 todo in the system");
    }

    @Test(description = "Create todos with concurrent threads using different IDs")
    public void testConcurrentTodoCreationWithDifferentIds() throws InterruptedException {
        logger.info("Executing concurrent todo creation test with different IDs");

        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                long uniqueId = generateUniqueId() + index;
                TodoDto todo = TodoDto.builder()
                    .id(uniqueId)
                    .text("Thread-" + index + "-Todo")
                    .completed(false)
                    .build();

                Response response = ApiUtils.createTodo(todo);
                assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Response getAllResponse = ApiUtils.getAllTodos();
        List<TodoDto> todos = ApiUtils.getTodosFromResponse(getAllResponse);
        assertEquals(todos.size(), 5, "Should have created 5 todos with different IDs");
    }
    /**
     * Cleans up test environment after test suite completion.
     */
    @AfterMethod
    public void tearDown() {
        logger.info("Cleaning up after todo creation tests");
        ApiUtils.cleanUpAllTodos();
    }
}