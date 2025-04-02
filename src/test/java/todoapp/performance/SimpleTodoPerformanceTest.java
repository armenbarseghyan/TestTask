package todoapp.performance;

import todoapp.api.BaseTest;
import todoapp.constants.ApiConstants;
import todoapp.dto.TodoDto;
import todoapp.utils.ApiUtils;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertTrue;

/**
 * Simple performance test suite for Todo API.
 * Tests creation, reading, updating, and deletion operations under concurrent load.
 */
public class SimpleTodoPerformanceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTodoPerformanceTest.class);
    
    // Test configuration
    private static final int CONCURRENT_USERS = 5;
    private static final int REQUESTS_PER_USER = 10;
    private static final int TIMEOUT_SECONDS = 30;
    
    private ExecutorService executor;
    
    @BeforeClass
    public void setUp() {
        logger.info("Setting up performance test");
        ApiUtils.initRestAssured();
        ApiUtils.cleanUpAllTodos();
        executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
    }
    
    @Test(description = "Test todo creation performance")
    public void testTodoCreationPerformance() throws InterruptedException {
        logger.info("Starting todo creation performance test");
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        String todoText = "Perf-Create-User" + userId + "-Todo" + j;
                        TodoDto todo = TodoDto.createTodoWithText(todoText);
                        
                        long requestStart = System.currentTimeMillis();
                        Response response = ApiUtils.createTodo(todo);
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        if (response.getStatusCode() == ApiConstants.STATUS_CREATED) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet(responseTime);
                            updateMaxResponseTime(maxResponseTime, responseTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in test thread: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(completed, "Performance test did not complete within timeout");
        
        long totalTime = System.currentTimeMillis() - startTime;
        logPerformanceResults("CREATE", successCount.get(), totalResponseTime.get(), 
                maxResponseTime.get(), totalTime);
    }
    
    @Test(description = "Test todo reading performance")
    public void testTodoReadingPerformance() throws InterruptedException {
        logger.info("Starting todo reading performance test");
        
        // Create test todos for reading
        List<Long> todoIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TodoDto todo = ApiUtils.createTestTodo("Perf-Read-Todo" + i);
            todoIds.add(todo.getId());
        }
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        // Get a todo ID using round-robin
                        Long todoId = todoIds.get(j % todoIds.size());
                        
                        long requestStart = System.currentTimeMillis();
                        Response response = ApiUtils.getTodoById(todoId);
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        if (response.getStatusCode() == ApiConstants.STATUS_OK) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet(responseTime);
                            updateMaxResponseTime(maxResponseTime, responseTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in test thread: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(completed, "Performance test did not complete within timeout");
        
        long totalTime = System.currentTimeMillis() - startTime;
        logPerformanceResults("READ", successCount.get(), totalResponseTime.get(), 
                maxResponseTime.get(), totalTime);
    }
    
    @Test(description = "Test todo updating performance")
    public void testTodoUpdatePerformance() throws InterruptedException {
        logger.info("Starting todo update performance test");
        
        // Create test todos for updating
        List<TodoDto> todos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TodoDto todo = ApiUtils.createTestTodo("Perf-Update-Todo" + i);
            todos.add(todo);
        }
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        // Get a todo using round-robin
                        TodoDto todo = todos.get(j % todos.size());
                        
                        // Create update DTO
                        TodoDto updateDto = TodoDto.builder()
                                .id(todo.getId())
                                .text("Updated by User" + userId + "-Req" + j)
                                .completed(!todo.getCompleted())
                                .build();
                        
                        long requestStart = System.currentTimeMillis();
                        Response response = ApiUtils.updateTodo(todo.getId(), updateDto);
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        if (response.getStatusCode() == ApiConstants.STATUS_OK) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet(responseTime);
                            updateMaxResponseTime(maxResponseTime, responseTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in test thread: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(completed, "Performance test did not complete within timeout");
        
        long totalTime = System.currentTimeMillis() - startTime;
        logPerformanceResults("UPDATE", successCount.get(), totalResponseTime.get(), 
                maxResponseTime.get(), totalTime);
    }
    
    @Test(description = "Test todo deletion performance")
    public void testTodoDeletionPerformance() throws InterruptedException {
        logger.info("Starting todo deletion performance test");
        
        // Create todos for each user to delete
        List<List<TodoDto>> userTodos = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            List<TodoDto> todos = new ArrayList<>();
            for (int j = 0; j < REQUESTS_PER_USER; j++) {
                TodoDto todo = ApiUtils.createTestTodo("Perf-Delete-User" + i + "-Todo" + j);
                todos.add(todo);
            }
            userTodos.add(todos);
        }
        
        CountDownLatch latch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong maxResponseTime = new AtomicLong(0);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    List<TodoDto> todosToDelete = userTodos.get(userId);
                    for (int j = 0; j < REQUESTS_PER_USER; j++) {
                        TodoDto todo = todosToDelete.get(j);
                        
                        long requestStart = System.currentTimeMillis();
                        Response response = ApiUtils.deleteTodo(todo.getId());
                        long responseTime = System.currentTimeMillis() - requestStart;
                        
                        if (response.getStatusCode() == ApiConstants.STATUS_NO_CONTENT) {
                            successCount.incrementAndGet();
                            totalResponseTime.addAndGet(responseTime);
                            updateMaxResponseTime(maxResponseTime, responseTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in test thread: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertTrue(completed, "Performance test did not complete within timeout");
        
        long totalTime = System.currentTimeMillis() - startTime;
        logPerformanceResults("DELETE", successCount.get(), totalResponseTime.get(), 
                maxResponseTime.get(), totalTime);
    }
    
    /**
     * Atomically updates the maximum response time if the current response time is larger.
     */
    private void updateMaxResponseTime(AtomicLong maxResponseTime, long responseTime) {
        long currentMax;
        do {
            currentMax = maxResponseTime.get();
            if (responseTime <= currentMax) break;
        } while (!maxResponseTime.compareAndSet(currentMax, responseTime));
    }
    
    /**
     * Logs performance test results.
     */
    private void logPerformanceResults(String operation, int successCount, long totalResponseTime,
                                     long maxResponseTime, long totalTime) {
        int totalRequests = CONCURRENT_USERS * REQUESTS_PER_USER;
        double successRate = (double) successCount / totalRequests * 100;
        double avgResponseTime = successCount > 0 ? (double) totalResponseTime / successCount : 0;
        double throughput = totalTime > 0 ? successCount / (totalTime / 1000.0) : 0;
        
        logger.info("----------------------------------------");
        logger.info("{} Performance Test Results:", operation);
        logger.info("----------------------------------------");
        logger.info("Total Requests: {}", totalRequests);
        logger.info("Successful Requests: {} ({:.2f}%)", successCount, successRate);
        logger.info("Failed Requests: {}", totalRequests - successCount);
        logger.info("Average Response Time: {:.2f} ms", avgResponseTime);
        logger.info("Maximum Response Time: {} ms", maxResponseTime);
        logger.info("Total Test Duration: {} ms", totalTime);
        logger.info("Throughput: {:.2f} requests/second", throughput);
        logger.info("----------------------------------------");
    }
    
    @AfterClass
    public void tearDown() {
        logger.info("Cleaning up after performance test");
        
        // Shut down executor service
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        // Clean up todos
        ApiUtils.cleanUpAllTodos();
    }
}
