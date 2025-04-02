package todoapp.api;

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
public class TodoCreateTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoCreateTest.class);



    /**
     * Verifies todo creation with valid, minimal data.
     */
    @Test(description = "Create todo with standard valid data")
    public void testCreateTodoWithValidData() {
        logger.info("Executing valid todo creation test");
        String uniqueText = generateUniqueText("Valid test todo");
        TodoDto todoToCreate = TodoDto.createTodoWithText(uniqueText);
        createAndVerifyTodo(todoToCreate, false);
    }

    /**
     * Verifies todo creation with completed status.
     */
    @Test(description = "Create todo with completed status")
    public void testCreateCompletedTodo() {
        logger.info("Executing completed todo creation test");
        String uniqueText = generateUniqueText("Completed todo");
        TodoDto todoToCreate = TodoDto.createCustomTodo(uniqueText, true);
        createAndVerifyTodo(todoToCreate, true);
    }

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
     * Verifies todo creation with empty text.
     */
    @Test(description = "Create todo with empty text")
    public void testCreateTodoWithEmptyText() {
        logger.info("Executing empty text todo creation test");
        long uniqueId = generateUniqueId();
        TodoDto todoToCreate = TodoDto.builder()
            .id(uniqueId)
            .text("")
            .completed(false)
            .build();

        Response response = ApiUtils.createTodo(todoToCreate);
        assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
            "Should allow todo creation with empty text");

        verifyTodoExistsById(uniqueId, "", false);
    }

    /**
     * Verifies creating multiple todos in succession.
     */
    @Test(description = "Create multiple todos")
    public void testCreateMultipleTodos() {
        logger.info("Executing multiple todo creation test");
        long baseTimestamp = generateUniqueId();
        String[] uniqueTexts = new String[5];

        // Create 5 todos
        for (int i = 1; i <= 5; i++) {
            uniqueTexts[i-1] = "Multiple creation test " + i + "_" + baseTimestamp;
            TodoDto todoToCreate = TodoDto.builder()
                .id(baseTimestamp + i)
                .text(uniqueTexts[i-1])
                .completed(false)
                .build();

            Response response = ApiUtils.createTodo(todoToCreate);
            assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
                "Should create todo " + i);
        }

        verifyMultipleTodosExist(uniqueTexts);
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
     * Verifies handling of todo creation with extra fields.
     */
    @Test(description = "Create todo with extra fields")
    public void testCreateTodoWithExtraFields() {
        logger.info("Executing todo creation with extra fields test");
        long uniqueId = generateUniqueId();
        String uniqueText = generateUniqueText("Todo with extra fields");

        Map<String, Object> todoWithExtraFields = new HashMap<>();
        todoWithExtraFields.put("id", uniqueId);
        todoWithExtraFields.put("text", uniqueText);
        todoWithExtraFields.put("completed", false);
        todoWithExtraFields.put("priority", "high");
        todoWithExtraFields.put("dueDate", "2025-04-15");

        Response response = ApiUtils.createTodoRaw(todoWithExtraFields);

        assertEquals(response.getStatusCode(), ApiConstants.STATUS_CREATED,
            "Should create todo with extra fields");

        verifyTodoExists(uniqueText, null);
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