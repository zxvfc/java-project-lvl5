package hexlet.code.controllerTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.api.DBRider;
import hexlet.code.dto.LabelDto;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.utils.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static hexlet.code.controller.LabelController.LABEL_PATH;
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
@DataSet("labels.yml")
public class LabelControllerTest {

    private final LabelDto labelDto = new LabelDto("newLabel");

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private LabelRepository labelRepository;

    @Test
    public void getLabelTest() throws Exception {
        final MockHttpServletResponse response = testUtils.perform(
                get(BASE_URL + LABEL_PATH + ID, 1), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("oneLabel");
    }

    @Test
    public void getAllLabelsTest() throws Exception {
        final long countLabelsInRepository = labelRepository.count();
        final MockHttpServletResponse response = testUtils.perform(
                        get(BASE_URL + LABEL_PATH), TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn().getResponse();

        List<Label> labels = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(labels.size()).isEqualTo(countLabelsInRepository);
    }

    @Test
    public void createLabelTest() throws Exception {
        final MockHttpServletResponse response = testUtils.perform(
                post(BASE_URL + LABEL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(labelDto)),
                        TEST_USERNAME)
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON.toString());
        assertThat(response.getContentAsString()).contains("newLabel");
    }

    @Test
    public void updateLabelTest() throws Exception {
        final MockHttpServletResponse response = testUtils.perform(
                        put(BASE_URL + LABEL_PATH + ID, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(labelDto)),
                        TEST_USERNAME)
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(labelRepository.getById(1L).getName()).isEqualTo(labelDto.getName());
    }

    @Test
    public void deleteLabelTest() throws Exception {

        assertThat(labelRepository.count()).isEqualTo(2);

        testUtils.perform(
                delete(BASE_URL + LABEL_PATH + ID, 2), TEST_USERNAME)
                .andExpect(status().isOk());

        assertThat(labelRepository.count()).isEqualTo(1);
    }

    @Test
    public void crudTestNegative() throws Exception {
        testUtils.perform(get(BASE_URL + LABEL_PATH + ID, 1))
                .andExpect(status().isUnauthorized());

        testUtils.perform(get(BASE_URL + LABEL_PATH))
                .andExpect(status().isUnauthorized());

        testUtils.perform(post(BASE_URL + LABEL_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(labelDto)))
                .andExpect(status().isUnauthorized());

        testUtils.perform(post(BASE_URL + LABEL_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new LabelDto(""))), TEST_USERNAME)
                .andExpect(status().isUnprocessableEntity());

        testUtils.perform(put(BASE_URL + LABEL_PATH + ID, 1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(labelDto)))
                .andExpect(status().isUnauthorized());

        testUtils.perform(delete(BASE_URL + LABEL_PATH + ID, 2))
                .andExpect(status().isUnauthorized());
    }
}
