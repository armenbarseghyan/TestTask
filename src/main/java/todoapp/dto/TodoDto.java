package todoapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Todo item in the application.
 * Provides methods for creating Todo instances with various configurations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodoDto {
    private Long id;
    private String text;
    private Boolean completed;

    /**
     * Creates a default Todo item for testing purposes.
     *
     * @return A new TodoDto with default test values
     */
    public static TodoDto createDefaultTodo() {
        return TodoDto.builder()
            .id(System.currentTimeMillis())
            .text("Test TODO")
            .completed(false)
            .build();
    }

    /**
     * Creates a Todo item with custom text for testing.
     *
     * @param text The text description of the Todo item
     * @return A new TodoDto with the specified text
     */
    public static TodoDto createTodoWithText(String text) {
        return TodoDto.builder()
            .id(System.currentTimeMillis())
            .text(text)
            .completed(false)
            .build();
    }

    /**
     * Creates a Todo item with custom text and completion status.
     *
     * @param text The text description of the Todo item
     * @param completed The completion status of the Todo item
     * @return A new TodoDto with the specified properties
     */
    public static TodoDto createCustomTodo(String text, boolean completed) {
        return TodoDto.builder()
            .id(System.currentTimeMillis())
            .text(text)
            .completed(completed)
            .build();
    }
}