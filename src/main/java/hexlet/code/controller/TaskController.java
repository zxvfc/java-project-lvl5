package hexlet.code.controller;

import com.querydsl.core.types.Predicate;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.NoSuchElementException;

import static hexlet.code.controller.TaskController.TASK_PATH;
import static hexlet.code.controller.UserController.ID;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("${base-url}" + TASK_PATH)
public class TaskController {

    public static final String TASK_PATH = "/tasks";
    private static final String ONLY_OWNER_BY_ID = """
            @taskRepository.findById(#id).get().getAuthor().getEmail() == authentication.getName()
        """;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskServiceImpl taskService;

    @Operation(summary = "Get Task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping(ID)
    public Task getTask(
            @Parameter(description = "Id of Task to be found", required = true)
            @PathVariable Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task with such id not found"));
    }

    @Operation(summary = "Get list of all Tasks")
    @ApiResponse(responseCode = "200", description = "List of all Tasks", content =
    @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class)))
    @GetMapping
    public Iterable<Task> getAllTasks(@QuerydslPredicate(root = Task.class) Predicate predicate) {
        return taskRepository.findAll(predicate);
    }

    @Operation(summary = "Creating new Task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "422", description = "Data validation failed")
    })
    @PostMapping
    @ResponseStatus(CREATED)
    public Task createTask(
            @Parameter(description = "Data for creating Task", required = true)
            @RequestBody @Valid TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @Operation(summary = "Update Task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = Task.class))),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "422", description = "Data validation failed")
    })
    @PutMapping(ID)
    public Task updateTask(
            @Parameter(description = "Id of Task to be updated", required = true)
            @PathVariable Long id,
            @Parameter(description = "Data for updating Task", required = true)
            @RequestBody @Valid TaskDto taskDto
    ) {
        return taskService.updateTask(id, taskDto);
    }

    @Operation(summary = "Delete task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted"),
            @ApiResponse(responseCode = "401", description = "User is unauthorized"),
            @ApiResponse(responseCode = "403", description = "Operation available only for owner")
    })
    @DeleteMapping(ID)
    @PreAuthorize(ONLY_OWNER_BY_ID)
    public void deleteTask(
            @Parameter(description = "Id of Task to be deleted")
            @PathVariable Long id
    ) {
        taskRepository.deleteById(id);
    }
}
