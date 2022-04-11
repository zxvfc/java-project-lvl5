package hexlet.code.controllerTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import hexlet.code.dto.TaskDto;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static hexlet.code.controller.TaskController.TASK_PATH;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.utils.TestUtils.fromJsom;
import static hexlet.code.utils.TestUtils.toJson;
import static hexlet.code.utils.TestUtils.TEST_USERNAME;
import static hexlet.code.utils.TestUtils.BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Transactional
@DBRider
@DBUnit(alwaysCleanBefore = true)
@DataSet("tasks.yml")
public class TaskControllerTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void getAllTasks() throws Exception {
        final MockHttpServletResponse response = testUtils.perform(
                get(BASE_URL + TASK_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentAsString()).contains("taskOne");
        assertThat(response.getContentAsString()).contains("taskTwo");
        List<Task> tasks = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(tasks.size()).isEqualTo(2);
    }

    @Test
    public void getAllTasksQueryDsl() throws Exception {
        String queryDsl = "?taskStatus=1&executorId=2";
        final MockHttpServletResponse response = testUtils.perform(
                        get(BASE_URL + TASK_PATH + queryDsl))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentAsString()).contains("taskOne");
        assertThat(response.getContentAsString()).doesNotContain("taskTwo");
        List<Task> tasks = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(tasks.size()).isEqualTo(1);
    }

    @Test
    public void getTask() throws Exception {
        final Task task = taskRepository.findById(1L).get();
        final MockHttpServletResponse response = testUtils.perform(
                get(BASE_URL + TASK_PATH + ID, task.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains(
                task.getName(),
                task.getDescription(),
                String.valueOf(task.getId()));
    }

    @Test
    public void createTask() throws Exception {

        assertThat(taskRepository.count()).isEqualTo(2);

        final TaskDto taskDto = new TaskDto(
                "create test task",
                "this is test",
                1,
                1,
                Set.of(1L)
        );
        final MockHttpServletRequestBuilder request = post(
                BASE_URL + TASK_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(taskDto));

        final MockHttpServletResponse response = testUtils.perform(request, TEST_USERNAME)
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains(
                taskDto.getName(),
                taskDto.getDescription(),
                String.valueOf(taskDto.getTaskStatusId()));

        assertThat(taskRepository.count()).isEqualTo(3);
    }

    @Test
    public void createTaskNegative() throws Exception {

        assertThat(taskRepository.count()).isEqualTo(2);

        final TaskDto taskDto = new TaskDto(
                "create test task",
                "this is test",
                1,
                1,
                Set.of(2L)
        );
        final MockHttpServletRequestBuilder request = post(
                BASE_URL + TASK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(taskDto));

        testUtils.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.count()).isEqualTo(2);
    }

    @Test
    public void updateTask() throws Exception {
        final TaskDto taskDto = new TaskDto(
                "update test task",
                "this is test",
                1,
                1,
                Set.of(2L)
        );
        final MockHttpServletRequestBuilder request = put(BASE_URL + TASK_PATH + ID, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(taskDto));

        final MockHttpServletResponse response = testUtils.perform(request, TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains(
                taskDto.getName(),
                taskDto.getDescription(),
                String.valueOf(taskDto.getTaskStatusId()));
        assertThat(taskRepository.getById(1L).getName()).isEqualTo(taskDto.getName());
    }

    @Test
    public void updateTaskNegative() throws Exception {
        final TaskDto taskDto = new TaskDto(
                "update test task",
                "this is test",
                1,
                1,
                Set.of()
        );
        final MockHttpServletRequestBuilder request = put(BASE_URL + TASK_PATH + ID, 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(taskDto));

        testUtils.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskRepository.getById(1L).getName()).isNotEqualTo(taskDto.getName());
    }

    @Test
    public void deleteTask() throws Exception {
        assertThat(taskRepository.count()).isEqualTo(2);

        testUtils.perform(delete(BASE_URL + TASK_PATH + ID, 1), TEST_USERNAME)
                .andExpect(status().isOk());

        assertThat(taskRepository.count()).isEqualTo(1);
    }

    @Test
    public void deleteTaskNegativeForbidden() throws Exception {
        assertThat(taskRepository.count()).isEqualTo(2);

        testUtils.perform(delete(BASE_URL + TASK_PATH + ID, 1), "Ivan@Ivan.com")
                .andExpect(status().isForbidden());

        assertThat(taskRepository.count()).isEqualTo(2);
    }
}
