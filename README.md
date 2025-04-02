# Todo API Test Automation Framework

## Overview

A comprehensive test automation solution for a RESTful Todo API with WebSocket notification support. This framework uses industry-standard approaches to verify API functionality across CRUD operations, WebSocket notifications, and performance under load.

## Technology Stack

- **Java 11** - Core programming language
- **TestNG** - Test execution framework with parallel test capabilities
- **REST-assured** - API testing client
- **Java WebSocket** - WebSocket client for notification testing
- **Allure** - Test reporting system
- **Maven** - Build and dependency management
- **Lombok** - Reduces boilerplate code

## Test Coverage

The framework provides extensive test coverage across multiple areas:

### CRUD Operations Coverage (40 Tests)

#### Create Operations (10 Tests)
- ✅ `testCreateTodoWithValidData` - Standard valid todo creation
- ✅ `testCreateCompletedTodo` - Creating todos with completed status
- ✅ `testCreateTodoWithExistingId` - Validation for duplicate ID prevention
- ✅ `testCreateTodoWithEmptyText` - Creation with empty text fields
- ✅ `testCreateMultipleTodos` - Multiple todo creation
- ✅ `testCreateTodoWithoutId` - Validation for missing ID field
- ✅ `testCreateTodoWithExtraFields` - Handling extra/unknown fields
- ✅ `testCreateTodoWithInvalidIdType` - Validation for incorrect ID type
- ✅ `testConcurrentTodoCreationWithSameId` - Race condition with duplicate ID
- ✅ `testConcurrentTodoCreationWithDifferentIds` - Concurrent creation with unique IDs

#### Read Operations (5 Tests)
- ✅ `testGetAllTodos` - Retrieving complete todo list
- ✅ `testGetTodoById` - Retrieving individual todos
- ✅ `testGetNonExistentTodo` - Handling non-existent todo requests
- ✅ `testResponseHeaders` - Validating API response headers
- ✅ `testGetAllWhenEmpty` - Handling empty todo list

#### Update Operations (6 Tests)
- ✅ `testUpdateTodoText` - Updating todo text field
- ✅ `testUpdateTodoCompletionStatus` - Updating completion status
- ✅ `testUpdateNonExistentTodo` - Handling updates for non-existent todos
- ✅ `testUpdateTodoWithMissingFields` - Validation for incomplete update data
- ✅ `testUpdateWithMismatchedIds` - Validation for ID mismatch between path and body
- ✅ `testCannotUpdateToExistingId` - Prevention of ID conflicts

#### Delete Operations (6 Tests)
- ✅ `testDeleteTodoWithAuth` - Deletion with proper authorization
- ✅ `testDeleteTodoWithoutProperAuth` - Authentication requirement validation
- ✅ `testDeleteNonExistentTodo` - Handling deletion of non-existent todos
- ✅ `testDeleteTodoAndVerifyInaccessible` - State verification after deletion
- ✅ `testDeleteTodoWithNegativeId` - Handling invalid ID formats
- ✅ `testDeleteMultipleTodos` - Multiple todo deletion

### WebSocket Integration (5 Tests)
- ✅ `testWebSocketConnection` - Connection establishment verification
- ✅ `testNotificationOnTodoCreation` - Creation notification validation
- ✅ `testMultipleNotifications` - Multiple notification handling
- ✅ `testNoNotificationForUpdates` - Verifies no updates for update operations
- ✅ `testNoNotificationForDeletes` - Verifies no updates for delete operations

### Performance Testing (4 Tests)
- ✅ `testTodoCreationPerformance` - Creation performance under concurrent load
- ✅ `testTodoReadingPerformance` - Retrieval performance under concurrent load
- ✅ `testTodoUpdatePerformance` - Update performance under concurrent load
- ✅ `testTodoDeletionPerformance` - Deletion performance under concurrent load

## Project Structure

```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── todoapp
│   │   │       ├── config
│   │   │       │   └── TestConfig.java               # Configuration management
│   │   │       ├── constants
│   │   │       │   └── ApiConstants.java             # API constants and endpoints
│   │   │       ├── dto
│   │   │       │   └── TodoDto.java                  # Data transfer objects
│   │   │       ├── listeners
│   │   │       │   └── AllureReportListener.java     # Allure reporting listener
│   │   │       └── utils
│   │   │           ├── ApiUtils.java                 # REST API utilities (18 methods)
│   │   │           └── WebSocketClient.java          # WebSocket client
│   │   └── resources
│   │       └── test.properties                       # Configuration properties
│   └── test
│       ├── java
│       │   └── todoapp
│       │       ├── api
│       │       │   ├── BaseTest.java                 # Common test functionality (7 methods)
│       │       │   ├── TodoCreateTest.java           # Creation tests (10 tests)
│       │       │   ├── TodoReadTest.java             # Read tests (5 tests)
│       │       │   ├── TodoUpdateTest.java           # Update tests (6 tests)
│       │       │   ├── TodoDeleteTest.java           # Delete tests (6 tests)
│       │       │   └── TodoWebSocketTest.java        # WebSocket tests (5 tests)
│       │       └── performance
│       │           └── SimpleTodoPerformanceTest.java # Performance tests (4 tests)
│       └── resources
│           ├── api_test_suite.xml                  # XML suite for bunch of tests run
│           └──test.properties                      # Configuration properties           
```

## API Testing Capabilities

The framework provides comprehensive coverage of the Todo API endpoints:

| Endpoint             | HTTP Method | Validation Points                                               |
|----------------------|-------------|-----------------------------------------------------------------|
| `/todos`             | GET         | Status code, content type, response format, empty list handling |
| `/todos/{id}`        | GET         | Status code, todo retrieval, non-existent ID handling           |
| `/todos`             | POST        | Status code, validation rules, concurrent creation              |
| `/todos/{id}`        | PUT         | Status code, update validation, ID mismatch detection           |
| `/todos/{id}`        | DELETE      | Status code, auth validation, non-existent ID handling          |
| WebSocket `/ws`      | N/A         | Connection, notifications, absence of notifications             |

### Key API Constants
The framework uses 11 API constants for standardization:
- `TODOS_ENDPOINT` - Base todos endpoint
- `TODO_BY_ID_ENDPOINT` - Endpoint for single todo operations
- `CONTENT_TYPE` - Content-Type header name
- `AUTHORIZATION` - Authorization header name
- `APPLICATION_JSON` - JSON content type value
- `LIMIT` and `OFFSET` - Pagination parameters
- Status codes: `STATUS_OK`, `STATUS_CREATED`, `STATUS_NO_CONTENT`, `STATUS_BAD_REQUEST`, `STATUS_UNAUTHORIZED`, `STATUS_NOT_FOUND`

## WebSocket Testing

The WebSocket testing capabilities include:
- Connection establishment and management
- Message reception and validation
- Notification verification for creation events
- Absence of notifications for update/delete events

## Performance Testing

Performance tests evaluate API behavior under concurrent load with:
- Concurrent user simulation (configurable number of users)
- Multiple operations per user (configurable number of requests)
- Response time tracking
- Throughput calculation
- Success rate monitoring

## Allure Reporting

The framework integrates Allure reporting for detailed test visualization:


### Generating Reports

```bash
# Run tests with Allure results
mvn clean test

# Generate the report
mvn allure:report

# Open the report in browser
mvn allure:serve
```

### Report Features
- Test execution history
- Detailed step breakdown
- Request/response logging
- Environment information
- Failure screenshots and logs
- Performance metrics visualization

## Test Data Management

The framework offers several approaches for test data creation:

### Todo Factory Methods
- `TodoDto.createDefaultTodo()` - Creates a standard todo
- `TodoDto.createTodoWithText(String text)` - Creates todo with specific text
- `TodoDto.createCustomTodo(String text, boolean completed)` - Creates todo with custom properties

### Helper Methods in BaseTest
- `createAndVerifyTodo(TodoDto, boolean)` - Creates and verifies a todo
- `verifyTodoExists(String, Boolean)` - Verifies todo by text
- `verifyTodoExistsById(long, String, boolean)` - Verifies todo by ID
- `verifyMultipleTodosExist(String[])` - Verifies multiple todos
- `generateUniqueId()` - Generates unique ID for tests
- `generateUniqueText(String)` - Generates unique text with prefix

## Setup and Configuration

### Prerequisites
- Java 11 or higher
- Maven 3.6.0 or higher
- Running Todo API instance

### Configuration Properties
Edit `src/main/resources/test.properties`:
```properties
# API Endpoints
base.url=http://localhost:9090
ws.url=ws://localhost:4242/ws

# Authentication
admin.username=admin
admin.password=admin
```

### TestConfig Methods
- `getBaseUrl()` - Returns the API base URL
- `getWsUrl()` - Returns the WebSocket URL
- `getAdminUsername()` - Returns admin username
- `getAdminPassword()` - Returns admin password

## Running Tests

### Running All Tests
```bash
mvn clean test
```

### Running Specific Test Groups
```bash
# Run only creation tests
mvn test -Dtest=TodoCreateTest

# Run only read tests
mvn test -Dtest=TodoReadTest

# Run only update tests
mvn test -Dtest=TodoUpdateTest

# Run only delete tests
mvn test -Dtest=TodoDeleteTest

# Run only WebSocket tests
mvn test -Dtest=TodoWebSocketTest

# Run only performance tests
mvn test -Dtest=SimpleTodoPerformanceTest
```

### Running with Custom Configuration
```bash
mvn test -Dbase.url=http://custom-host:9090 -Dws.url=ws://custom-host:4242/ws
```

## API Utils Capabilities

The `ApiUtils` class provides 18 methods for API interaction:

- Core Methods:
    - `initRestAssured()` - Initializes REST-assured
    - `getAllTodos()` - Gets all todos
    - `getTodoById(long)` - Gets a todo by ID
    - `createTodo(TodoDto)` - Creates a new todo
    - `updateTodo(long, TodoDto)` - Updates a todo
    - `deleteTodo(long)` - Deletes a todo

- Helper Methods:
    - `createTestTodo(String)` - Creates a test todo
    - `getTodosFromResponse(Response)` - Extracts todos from response
    - `cleanUpAllTodos()` - Cleans up all todos

- Advanced Methods:
    - `createTodoRaw(Map<String, Object>)` - Creates with raw data
    - `updateTodoRaw(long, Map<String, Object>)` - Updates with raw data
    - `getTodosWithPagination(int, int)` - Gets paginated todos
    - `deleteTodoWithoutAuth(long)` - Deletes without auth
    - `getBaseRequest()` - Gets base request specification
    - `getBasicAuthHeader()` - Gets basic auth header

## WebSocket Client Features

The WebSocket client provides 9 key methods:
- `reconnect()` - Reconnects to WebSocket
- `onOpen()`, `onMessage()`, `onClose()`, `onError()` - Event handlers
- `waitForMessages(int, int)` - Waits for specific message count
- `getReceivedMessages()` - Gets received messages
- `clearMessages()` - Clears message buffer
- `isConnected()` - Checks connection status

## Future Improvements

Planned enhancements for the framework:

1. **Concurrency Improvements**
    - Replace direct thread usage with CompletableFuture
    - Implement non-blocking WebSocket client

2. **Additional Test Types**
    - Security testing for authentication and authorization
    - Negative testing with boundary value analysis

3. **Framework Enhancements**
    - Custom Allure reporting extensions
    - Logging improvements for better debugging
    - Test data management via factories

4. **CI/CD Integration**
    - Jenkins pipeline configuration
    - Docker-based execution environment
    - Automated report publishing

## Conclusion

This framework provides comprehensive testing of the Todo API with:
- 42 automated tests covering CRUD operations, WebSockets and performance
- Detailed reporting with Allure
- Flexible configuration for different environments
- Structured project organization for maintainability