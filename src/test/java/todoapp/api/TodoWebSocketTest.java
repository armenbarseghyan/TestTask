package todoapp.api;

import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import todoapp.utils.WebSocketClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.testng.Assert.*;

/**
 * Comprehensive test suite for WebSocket functionality.
 * Validates real-time notifications for todo operations.
 */
public class TodoWebSocketTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(TodoWebSocketTest.class);
    private WebSocketClient wsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Prepares WebSocket connection and test environment.
     */
    @BeforeClass
    public void setUp() throws URISyntaxException, InterruptedException {
        logger.info("Initializing WebSocket test suite");
        ApiUtils.initRestAssured();

        // Establish WebSocket connection
        wsClient = WebSocketClient.createDefault();
        wsClient.connectBlocking(5, TimeUnit.SECONDS);

        assertTrue(wsClient.isOpen(), "WebSocket connection should be established");
    }

    /**
     * Verifies WebSocket connection is successfully established.
     */
    @Test(description = "Validate WebSocket connection")
    public void testWebSocketConnection() {
        assertTrue(wsClient.isConnected(), "WebSocket should be active");
    }

    /**
     * Verifies WebSocket notification is sent when a new todo is created.
     */
    @Test(description = "Receive notification on todo creation")
    public void testNotificationOnTodoCreation() throws Exception {
        // Clear previous messages
        wsClient.clearMessages();

        // Create a todo with unique identifier
        String uniqueText = "WebSocket notification test " + System.currentTimeMillis();
        TodoDto todoToCreate = TodoDto.builder()
            .id(System.currentTimeMillis())
            .text(uniqueText)
            .completed(false)
            .build();

        // Create todo
        ApiUtils.createTodo(todoToCreate);

        // Wait for message processing
        Thread.sleep(1000);

        // Validate received messages
        List<String> messages = wsClient.getReceivedMessages();

        assertFalse(messages.isEmpty(), "Should receive WebSocket message after todo creation");

        if (!messages.isEmpty()) {
            String message = messages.get(0);
            assertNotNull(message, "WebSocket message should not be null");

            // Validate message structure
            assertTrue(message.contains("\"type\":\"new_todo\""),
                "Message should indicate new todo creation");
            assertTrue(message.contains("\"id\":"), "Message should include todo ID");
            assertTrue(message.contains("\"text\":"), "Message should include todo text");
            assertTrue(message.contains("\"completed\":"), "Message should include completion status");
            assertTrue(message.contains(uniqueText), "Message should contain created todo text");
        }
    }

    /**
     * Verifies multiple notifications are sent for multiple todo creations.
     */
    @Test(description = "Receive notifications for multiple todo creations")
    public void testMultipleNotifications() throws Exception {
        // Clear previous messages
        wsClient.clearMessages();

        // Create multiple todos
        int todoCount = 3;
        for (int i = 1; i <= todoCount; i++) {
            TodoDto todoToCreate = TodoDto.builder()
                .id(System.currentTimeMillis() + i)
                .text("Multiple notification test " + i)
                .completed(false)
                .build();

            ApiUtils.createTodo(todoToCreate);

            // Small delay to avoid race conditions
            Thread.sleep(200);
        }

        // Wait for all WebSocket messages
        await().atMost(5, TimeUnit.SECONDS).until(() ->
            wsClient.getReceivedMessages().size() >= todoCount
        );

        // Verify message count
        List<String> messages = wsClient.getReceivedMessages();
        assertTrue(messages.size() >= todoCount,
            "Should receive at least " + todoCount + " messages, got " + messages.size());
    }

    /**
     * Verifies no notifications are sent for todo update operations.
     */
    @Test(description = "Confirm no notifications for todo updates")
    public void testNoNotificationForUpdates() throws Exception {
        // Create a todo to update
        TodoDto todo = ApiUtils.createTestTodo("Update notification test todo");

        // Wait for the creation notification and clear messages
        await().atMost(3, TimeUnit.SECONDS).until(() ->
            !wsClient.getReceivedMessages().isEmpty()
        );
        wsClient.clearMessages();

        // Update the todo
        todo.setText("Updated text for notification test");
        ApiUtils.updateTodo(todo.getId(), todo);

        // Wait a moment to see if any message arrives
        Thread.sleep(2000);

        // Verify no messages were received
        List<String> messages = wsClient.getReceivedMessages();
        assertEquals(messages.size(), 0,
            "Should not receive any messages for update operations");
    }

    /**
     * Verifies no notifications are sent for todo deletion operations.
     */
    @Test(description = "Confirm no notifications for todo deletions")
    public void testNoNotificationForDeletes() throws Exception {
        // Create a todo to delete
        TodoDto todo = ApiUtils.createTestTodo("Delete notification test todo");

        // Wait for the creation notification and clear messages
        await().atMost(3, TimeUnit.SECONDS).until(() ->
            !wsClient.getReceivedMessages().isEmpty()
        );
        wsClient.clearMessages();

        // Delete the todo
        ApiUtils.deleteTodo(todo.getId());

        // Wait a moment to see if any message arrives
        Thread.sleep(2000);

        // Verify no messages were received
        List<String> messages = wsClient.getReceivedMessages();
        assertEquals(messages.size(), 0,
            "Should not receive any messages for delete operations");
    }

    /**
     * Cleans up WebSocket connection and test resources after test suite.
     */
    @AfterClass
    public void tearDown() throws Exception {
        logger.info("Cleaning up WebSocket test suite resources");

        // Close WebSocket connection
        if (wsClient != null && wsClient.isOpen()) {
            wsClient.closeBlocking();
        }

        // Clean up todos
        ApiUtils.cleanUpAllTodos();
    }
}