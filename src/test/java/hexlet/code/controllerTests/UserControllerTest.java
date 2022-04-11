package hexlet.code.controllerTests;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.dto.LoginDto;
import hexlet.code.dto.UserDto;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
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

import static hexlet.code.config.security.SecurityConfig.LOGIN;
import static hexlet.code.controller.UserController.ID;
import static hexlet.code.controller.UserController.USER_CONTROLLER_PATH;
import static hexlet.code.utils.TestUtils.fromJsom;
import static hexlet.code.utils.TestUtils.toJson;
import static hexlet.code.utils.TestUtils.TEST_USERNAME;
import static hexlet.code.utils.TestUtils.TEST_USERNAME2;
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
public class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtils testUtils;

    @Test
    public void registrationTest() throws Exception {
        assertThat(userRepository.count()).isEqualTo(0);
        testUtils.regDefaultUser().andExpect(status().isCreated());
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void getUserTest() throws Exception {
        testUtils.regDefaultUser();
        final User expectedUser = userRepository.findAll().get(0);
        final MockHttpServletResponse response = testUtils.perform(
                        get(BASE_URL + USER_CONTROLLER_PATH + ID, expectedUser.getId()),
                        expectedUser.getEmail()
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final User user = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(user.getId()).isEqualTo(expectedUser.getId());
        assertThat(user.getEmail()).isEqualTo(expectedUser.getEmail());
        assertThat(user.getFirstName()).isEqualTo(expectedUser.getFirstName());
        assertThat(user.getLastName()).isEqualTo(expectedUser.getLastName());
    }

    @Test
    public void getUserNegative() throws Exception {
        testUtils.regDefaultUser();
        final User expectedUser = userRepository.findAll().get(0);
        testUtils.perform(get(BASE_URL + USER_CONTROLLER_PATH + ID, expectedUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAllUsers() throws Exception {
        testUtils.regDefaultUser();
        final MockHttpServletResponse response = testUtils.perform(get(BASE_URL + USER_CONTROLLER_PATH))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        final List<User> users = fromJsom(response.getContentAsString(), new TypeReference<>() {
        });

        assertThat(users.size()).isEqualTo(1);
    }

    @Test
    public void registrationTestTwiceNegative() throws Exception {
        testUtils.regDefaultUser().andExpect(status().isCreated());
        testUtils.regDefaultUser().andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    public void login() throws Exception {
        testUtils.regDefaultUser();
        final LoginDto loginDto = new LoginDto(
                testUtils.getTestRegistrationDto().getEmail(),
                testUtils.getTestRegistrationDto().getPassword());

        testUtils.perform(post(BASE_URL + LOGIN).content(toJson(loginDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void loginNegative() throws Exception {
        testUtils.regDefaultUser();
        final LoginDto loginDto = new LoginDto(
                testUtils.getTestRegistrationDto2().getEmail(),
                testUtils.getTestRegistrationDto2().getPassword());

        testUtils.perform(post(BASE_URL + LOGIN).content(toJson(loginDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateUserTest() throws Exception {
        testUtils.regDefaultUser();

        final Long id = userRepository.findByEmail(TEST_USERNAME).get().getId();

        final UserDto userDto = new UserDto(TEST_USERNAME2, "name", "lastname", "password");

        final MockHttpServletRequestBuilder updateRequest = put(BASE_URL + USER_CONTROLLER_PATH + ID, id)
                .content(toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON);

        testUtils.perform(updateRequest, TEST_USERNAME).andExpect(status().isOk());

        assertThat(userRepository.findByEmail(TEST_USERNAME2)).isNotNull();
        assertThat(userRepository.findByEmail(TEST_USERNAME2).get().getId()).isEqualTo(id);
    }

    @Test
    public void updateUserTestNegative() throws Exception {
        testUtils.regDefaultUser();

        final Long id = userRepository.findByEmail(TEST_USERNAME).get().getId();

        final UserDto userDto = new UserDto(TEST_USERNAME2, "name", "lastname", "password");

        final MockHttpServletRequestBuilder updateRequest = put(BASE_URL + USER_CONTROLLER_PATH + ID, id)
                .content(toJson(userDto))
                .contentType(MediaType.APPLICATION_JSON);

        testUtils.perform(updateRequest, TEST_USERNAME2).andExpect(status().isForbidden());
    }

    @Test
    public void deleteUserTest() throws Exception {
        testUtils.regDefaultUser();

        final Long id = userRepository.findByEmail(TEST_USERNAME).get().getId();

        final MockHttpServletRequestBuilder deleteRequest = delete(BASE_URL + USER_CONTROLLER_PATH + ID, id);
        testUtils.perform(deleteRequest, TEST_USERNAME).andExpect(status().isOk());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    public void deleteUserTestNegative() throws Exception {
        testUtils.regDefaultUser();

        final Long id = userRepository.findByEmail(TEST_USERNAME).get().getId();

        final MockHttpServletRequestBuilder deleteRequest = delete(BASE_URL + USER_CONTROLLER_PATH + ID, id);
        testUtils.perform(deleteRequest, TEST_USERNAME2).andExpect(status().isForbidden());

        assertThat(userRepository.count()).isEqualTo(1);
    }
}
