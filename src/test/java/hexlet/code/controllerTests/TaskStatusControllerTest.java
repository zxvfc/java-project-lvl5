package hexlet.code.controllerTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import hexlet.code.dto.TaskStatusDto;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static hexlet.code.controller.TaskStatusController.TASK_STATUS_PATH;
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
@DataSet("taskStatuses.yml")
public class TaskStatusControllerTest {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private TestUtils testUtils;

    @BeforeEach
    public void before() throws Exception {
        testUtils.regDefaultUser();
    }

    @Test
    public void createStatusTest() throws Exception {
        assertThat(statusRepository.count()).isEqualTo(3);

        String statusName = "in process";
        final MockHttpServletRequestBuilder request = post(BASE_URL + TASK_STATUS_PATH)
                .content(toJson(statusName))
                .contentType(MediaType.APPLICATION_JSON);

        final MockHttpServletResponse response = testUtils.perform(request, TEST_USERNAME)
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse();

        assertThat(statusRepository.count()).isEqualTo(4);
        assertThat(response.getContentAsString()).contains(statusName);
    }

    @Test
    public void createStatusTestNegative() throws Exception {
        assertThat(statusRepository.count()).isEqualTo(3);

        String statusName = "in process";
        final MockHttpServletRequestBuilder request = post(BASE_URL + TASK_STATUS_PATH)
                .content(toJson(statusName))
                .contentType(MediaType.APPLICATION_JSON);

        final MockHttpServletResponse response = testUtils.perform(request)
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse();

        assertThat(statusRepository.count()).isEqualTo(3);
        assertThat(response.getContentAsString()).doesNotContain(statusName);
    }

    @Test
    public void getStatusTest() throws Exception {
        final TaskStatus statusTask = statusRepository.findAll().get(0);
        final MockHttpServletResponse response = testUtils.perform(
                get(BASE_URL + TASK_STATUS_PATH + ID, statusTask.getId())
        ).andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains(statusTask.getName());
    }

    @Test
    public void getStatusesTest() throws Exception {
        long countStatuses = statusRepository.count();
        final MockHttpServletResponse response = testUtils.perform(
                        get(BASE_URL + TASK_STATUS_PATH)
                ).andExpect(status().isOk())
                .andReturn()
                .getResponse();
        List<TaskStatus> statuses = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(statuses.size()).isEqualTo(countStatuses);
    }

    @Test
    public void updateStatusTest() throws Exception {
        final TaskStatus statusTask = statusRepository.findAll().get(0);
        final TaskStatusDto taskStatusDto = new TaskStatusDto("finished");
        final MockHttpServletRequestBuilder request = put(
                BASE_URL + TASK_STATUS_PATH + ID, statusTask.getId())
                .content(toJson(taskStatusDto))
                .contentType(MediaType.APPLICATION_JSON);

        final MockHttpServletResponse response = testUtils.perform(request, TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        assertThat(response.getContentAsString()).contains("finished");
        assertThat(response.getContentAsString()).contains(String.valueOf(statusTask.getId()));
    }

    @Test
    public void updateStatusNegativeTest() throws Exception {
        final TaskStatus statusTask = statusRepository.findAll().get(0);
        final TaskStatusDto taskStatusDto = new TaskStatusDto("finished");
        final MockHttpServletRequestBuilder request = put(
                BASE_URL + TASK_STATUS_PATH + ID, statusTask.getId())
                .content(toJson(taskStatusDto))
                .contentType(MediaType.APPLICATION_JSON);

        testUtils.perform(request)
                .andExpect(status().isUnauthorized());
        final String statusName = statusRepository.findById(statusTask.getId()).get().getName();
        assertThat(statusName).isNotEqualTo("finished");
    }

    @Test
    public void deleteTaskTest() throws Exception {
        final long id = statusRepository.findAll().get(0).getId();
        testUtils.perform(
                delete(BASE_URL + TASK_STATUS_PATH + ID, id),
                TEST_USERNAME)
                .andExpect(status().isOk());

        assertThat(statusRepository.count()).isEqualTo(2);
    }

    @Test
    public void deleteTaskNegativeTest() throws Exception {
        final long id = statusRepository.findAll().get(0).getId();
        testUtils.perform(
                        delete(BASE_URL + TASK_STATUS_PATH + ID, id))
                .andExpect(status().isUnauthorized());

        assertThat(statusRepository.count()).isEqualTo(3);
    }
}
