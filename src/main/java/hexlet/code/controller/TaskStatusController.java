package hexlet.code.controller;

import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import java.util.List;
import javax.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import static hexlet.code.controller.TaskStatusController.TASK_STATUS_CONTROLLER_PATH;
import static org.springframework.http.HttpStatus.CREATED;

@AllArgsConstructor
@RestController
@SecurityRequirement(name = "javainuseapi")
@RequestMapping("${base-url}" + TASK_STATUS_CONTROLLER_PATH)
public class TaskStatusController {

    public static final String TASK_STATUS_CONTROLLER_PATH = "/statuses";
    public static final String ID = "/{id}";


    private final TaskStatusRepository taskStatusRepository;

    @GetMapping
    public List<TaskStatus> getAll() {
        return taskStatusRepository.findAll();
    }

    @Operation(summary = "Get state by Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "State found"),
            @ApiResponse(responseCode = "404", description = "State with that id not found")
    })
    @GetMapping(ID)
    public TaskStatus getById(@PathVariable final Long id) {
        return taskStatusRepository.findById(id).get();
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public TaskStatus createNew(@RequestBody @Valid final TaskStatusDto dto) {
        return taskStatusRepository.save(TaskStatus.builder()
                                                 .name(dto.getName())
                                                 .build()
        );
    }

    @PutMapping(ID)
    public TaskStatus updateState(@PathVariable final long id, @RequestBody @Valid final TaskStatusDto dto) {
        final TaskStatus taskStatusToUpdate = taskStatusRepository.findById(id).get();
        taskStatusToUpdate.setName(dto.getName());
        return taskStatusRepository.save(taskStatusToUpdate);
    }

    @DeleteMapping(ID)
    public void delete(@PathVariable final long id) {
        taskStatusRepository.deleteById(id);
    }

}
