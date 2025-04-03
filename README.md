# Todo API Test Automation Framework

## Overview

A comprehensive test automation solution for a RESTful Todo API with WebSocket notification support. This framework uses industry-standard approaches to verify API functionality across CRUD operations, WebSocket notifications, and performance under load.

## Technology Stack

- **Java 17** - Core programming language with modern features like records, enhanced switch expressions, and text blocks
- **TestNG** - Test execution framework with robust test organization
- **REST-assured** - API testing client
- **Java WebSocket** - WebSocket client for notification testing
- **Allure** - Test reporting system
- **Maven** - Build and dependency management
- **Lombok** - Reduces boilerplate code
- **SLF4J** - Comprehensive logging framework

## Test Coverage

The framework provides focused test coverage across multiple areas:

### CRUD Operations Coverage (Core 20 Tests)

#### Create Operations (5 Core Tests)
- ✅ `testCreateTodoWithExistingId` - Validation for duplicate ID prevention
- ✅ `testCreateTodoWithoutId` - Validation for missing ID field
- ✅ `testCreateTodoWithInvalidIdType` - Validation for incorrect ID type
- ✅ `testConcurrentTodoCreationWithSameId` - Race condition with duplicate ID
- ✅ `testConcurrentTodoCreationWithDifferentIds` - Concurrent creation with unique IDs

#### Read Operations (5 Tests)
- ✅ `testGetAllTodos` - Retrieving complete todo list
- ✅ `testGetTodoById` - Retrieving individual todos
- ✅ `testGetNonExistentTodo` - Handling non-existent todo requests
- ✅ `testResponseHeaders` - Validating API response headers
- ✅ `testGetAllWhenEmpty` - Handling empty todo list

#### Update Operations (5 Core Tests)
- ✅ `testUpdateTodoText` - Updating todo text field
- ✅ `testUpdateTodoCompletionStatus` - Updating completion status
- ✅ `testUpdateNonExistentTodo` - Handling updates for non-existent todos
- ✅ `testUpdateTodoWithMissingFields` - Validation for incomplete update data
- ✅ `testUpdateWithMismatchedIds` - Validation for ID mismatch between path and body

#### Delete Operations (5 Core Tests)
- ✅ `testDeleteTodoWithAuth` - Deletion with proper authorization
- ✅ `testDeleteTodoWithoutProperAuth` - Authentication requirement validation
- ✅ `testDeleteNonExistentTodo` - Handling deletion of non-existent todos
- ✅ `testDeleteTodoAndVerifyInaccessible` - State verification after deletion
- ✅ `testDeleteTodoWithNegativeId` - Handling invalid ID formats

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
│       │       │   ├── BaseTest.java                 # Enhanced common test functionality
│       │       │   ├── TodoCreateTest.java           # Creation tests (5 core tests)
│       │       │   ├── TodoReadTest.java             # Read tests (5 tests)
│       │       │   ├── TodoUpdateTest.java           # Update tests (5 core tests)
│       │       │   ├── TodoDeleteTest.java           # Delete tests (5 core tests)
│       │       │   └── TodoWebSocketTest.java        # WebSocket tests (5 tests)
│       │       └── performance
│       │           └── SimpleTodoPerformanceTest.java # Performance tests (4 tests)
│       └── resources
│           ├── api_test_suite.xml                           # Optimized TestNG configuration
│           └── test.properties                       # Test-specific properties           
```

## Enhanced Base Test Class

The framework features an improved `BaseTest` class that provides:

### Setup and Teardown
- `suiteSetup()` - One-time initialization of REST-assured and logging
- `baseSetup()` - Per-test setup with robust error handling
- `baseTearDown()` - Reliable test cleanup with improved error handling

### Core Helper Methods
- `createAndVerifyTodo(TodoDto, boolean)` - Creates and verifies a todo with comprehensive logging
- `verifyTodoExists(String, Boolean)` - Enhanced todo verification with detailed assertions
- `verifyTodoExistsById(long, String, boolean)` - ID-based todo verification with better error messages
- `verifyMultipleTodosExist(String[])` - Multiple todo verification with enhanced logging
- `verifyNegativeScenario(TodoDto, String)` - Robust negative test validation

### Utility Methods
- `generateUniqueId()` - Creates unique IDs for test data
- `generateUniqueText(String)` - Generates unique text with standardized format
- `getCallingMethodName()` - Provides test context for improved logging

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

The framework integrates Allure reporting for comprehensive test visualization and analysis:

### Allure Implementation

The framework implements Allure reporting through multiple components:

1. **Listeners Configuration**
    - `AllureReportListener.java` - Custom TestNG listener for enhanced reporting
    - `io.qameta.allure.testng.AllureTestNg` - Core Allure TestNG integration
    - XML configuration in the TestNG configuration file

2. **Annotations Usage**
    - `@Epic` and `@Feature` annotations for test organization
    - `@Step` annotations for detailed test steps documentation
    - `@Description` annotations for test purpose clarity

3. **Integration with REST-assured**
    - Allure filter for capturing HTTP requests and responses
    - Automatic attachment of request/response details
    - Error details capture for failed API calls

### Report Content

The Allure reports generated by the framework include:

- **Test Results Dashboard** - Overview of passed, failed, and skipped tests
- **Test Suites Breakdown** - Organized view by test category (CRUD, WebSocket, Performance)
- **Detailed Test Steps** - Hierarchical view of test execution steps
- **API Call Details** - Request/response information for each API interaction
- **Environment Information** - Test environment details and configuration values
- **Execution Timeline** - Chronological view of test execution
- **Failure Analysis** - Detailed error information and stack traces
- **Test History** - Test execution trends over time (when integrated with CI/CD)

### Generating Reports

```bash
# Run tests with Allure results collection
mvn clean test

# Generate the HTML report
mvn allure:report

# Open the report in browser
mvn allure:serve
```




When integrated with CI/CD, the Allure report provides:
- Historical trends of test execution
- Comparison between builds
- Failure rate analysis
- Test stability metrics

## Test Data Management

The framework offers several approaches for test data creation:

### Todo Factory Methods
- `TodoDto.createDefaultTodo()` - Creates a standard todo
- `TodoDto.createTodoWithText(String text)` - Creates todo with specific text
- `TodoDto.createCustomTodo(String text, boolean completed)` - Creates todo with custom properties

### Enhanced Helper Methods in BaseTest
- `createAndVerifyTodo(TodoDto, boolean)` - Creates and verifies a todo with improved logging
- `verifyTodoExists(String, Boolean)` - Verifies todo by text with detailed status feedback
- `verifyTodoExistsById(long, String, boolean)` - Verifies todo by ID with comprehensive logging
- `verifyMultipleTodosExist(String[])` - Verifies multiple todos with enhanced error reporting
- `generateUniqueId()` - Generates unique ID for tests
- `generateUniqueText(String)` - Generates unique text with prefix

## Optimized TestNG Configuration

The framework includes an optimized TestNG configuration file (`testng.xml`) that:

- Focuses on 5 core tests for each CRUD operation
- Explicitly lists each test method to be executed
- Separates tests into logical groups (CRUD and WebSocket)
- Includes proper listener configuration for reporting
- Provides global parameters for timeout settings

## Setup and Configuration

### Prerequisites
- **Java 17** or higher - Required for modern language features used in the framework
- **Maven 3.8.6** or higher - For dependency management and build automation
- **Running Todo API instance** - Must be accessible via HTTP with proper endpoints configured

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

The framework has a clear roadmap for future enhancements:

### 1. Enhanced Test Data Management
- **Data Factory Pattern Implementation**: Create dedicated `TodoTestDataFactory` class with methods for generating complex test data scenarios
- **External Data Sources**: Add support for loading test data from CSV, JSON, and Excel files
- **Data Randomization Library**: Integrate a library like JavaFaker for generating realistic random data
- **Test Data Cleanup Tracking**: Implement a registry system that tracks all created test data for reliable cleanup

### 2. API Client Architecture Redesign
- **Object-Oriented Client**: Replace static ApiUtils with a fluent, object-oriented `TodoApiClient` class
- **Builder Pattern**: Implement a builder pattern for flexible client configuration
- **Request/Response Models**: Create proper model classes for request/response payloads
- **Authentication Strategies**: Support multiple authentication mechanisms with strategy pattern
- **Retry Mechanism**: Add intelligent retry for handling transient network issues

### 3. Advanced Error Handling Framework
- **Custom Exception Hierarchy**: Develop a domain-specific exception hierarchy (e.g., `TodoApiException`, `ValidationException`)
- **Enriched Error Context**: Capture and include detailed request/response data with exceptions
- **AssertJ Integration**: Replace TestNG assertions with AssertJ for more readable and powerful assertions
- **Soft Assertions**: Implement soft assertions to collect multiple failures in a single test run

### 4. Comprehensive Logging Strategy
- **Structured Logging**: Implement JSON-formatted structured logging
- **Request/Response Logging**: Add custom logging filter for detailed API interaction logging
- **Log Correlation**: Add correlation IDs across test execution for traceability
- **Log Level Configuration**: Create environment-specific logging profiles

### 5. Advanced Testing Capabilities
- **Contract Testing**: Add OpenAPI/Swagger validation support
- **Schema Validation**: Implement JSON Schema validation for response payloads
- **Performance Thresholds**: Add configurable performance thresholds for automated performance testing
- **Security Testing**: Add security validation for authentication and authorization flows

### 6. CI/CD Pipeline Integration
- **Jenkins Pipeline**: Create Jenkinsfile with full pipeline definition
- **Docker Containerization**: Add Docker support for consistent test execution environments
- **Parallel Execution**: Configure efficient parallelization strategy for CI environments
- **Automated Reporting**: Set up automated report generation and distribution
- **Slack/Teams Notifications**: Add instant messaging notifications for test results

## Conclusion

This framework provides comprehensive testing of the Todo API with:
- 30 core tests covering all aspects of the API functionality
- Improved test organization focusing on 5 core tests per API operation
- Enhanced BaseTest class with better logging and error handling
- Optimized TestNG configuration for focused test execution
- Detailed reporting with Allure
- Flexible configuration for different environments
- Structured project organization for maintainability