# Bug Report: Todo API Testing

## Overview
This report outlines issues discovered during automated API testing of the Todo application. The identified issues primarily involve authentication requirements, validation inconsistencies, and improper error handling in the API.

## Issues Identified

### Issue 1: GET Operations Require Authentication
- **Test Cases:** `testGetTodoById` and `testGetNonExistentTodo` (TodoReadTest)
- **Description:** The API requires authentication for GET operations on individual todos, returning `401 Unauthorized` when accessing a specific todo by ID.
- **Expected Behavior:** GET operations should not require authentication as per the API specification.
- **Actual Behavior:** The API returns a `401 Unauthorized` status code for GET requests to `/todos/{id}`.
- **Severity:** High - Core functionality is affected.
- **Recommendation:** Remove authentication requirement for the `GET /todos/{id}` endpoint.

### Issue 2: No Validation for Mismatched IDs in Update Requests
- **Test Cases:** `testCannotUpdateToExistingId` and `testUpdateWithMismatchedIds` (TodoUpdateTest)
- **Description:** The API accepts update requests even when the ID in the request body does not match the ID in the URL path.
- **Expected Behavior:** A `PUT` request where the ID in the body differs from the ID in the URL should be rejected with a `400 Bad Request`.
- **Actual Behavior:** The API accepts the update (`200 OK`), despite the mismatch, which poses a risk to data integrity.
- **Severity:** High - Potential data integrity issues.
- **Recommendation:** Add validation to ensure the ID in the request body matches the ID in the URL path.

### Issue 3: Authentication Before Validation in Update Requests
- **Test Cases:** `testUpdateTodoWithMissingFields` (TodoUpdateTest)
- **Description:** The API returns a `401 Unauthorized` error when attempting to update a todo with missing required fields, rather than validating the request and returning an appropriate error.
- **Expected Behavior:** The API should validate the request body first and return `400 Bad Request` for missing required fields.
- **Actual Behavior:** The API checks authentication before validating the request, returning `401 Unauthorized` even for malformed requests.
- **Severity:** Medium - Improper error handling.
- **Recommendation:** Implement request validation before authentication checks to provide more accurate error responses.

## Summary of Issues
1. **Authentication Configuration:** GET operations incorrectly require authentication, contrary to the API specification.
2. **Missing Validation:** The API does not validate mismatched IDs in PUT operations, potentially causing data integrity issues.
3. **Error Handling Sequence:** Authentication checks occur before request validation, leading to misleading error responses.

These issues impact the reliability and data integrity of the Todo API. Addressing them will improve API behavior and prevent potential inconsistencies.



