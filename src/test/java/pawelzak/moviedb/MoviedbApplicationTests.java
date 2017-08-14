package pawelzak.moviedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MoviedbApplicationTests {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper mapper = new ObjectMapper();

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Before
  public void setup() {
    userRepository.deleteAll();
  }

  @Test
  public void testCreateUserSuccessfully() throws Exception {
    UserCreateRequest user = UserCreateRequest.builder()
      .email("email@company.com")
      .password("0123456789")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(user))
      )
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").isString());

    Assertions.assertThat(userRepository.findAllByEmail("email@company.com")).hasSize(1);
  }

  @Test
  public void testCreateExistingUserFails() throws Exception {
    User user = User.builder()
      .email("email@company.com")
      .password("0123456789")
      .build();

    userRepository.saveAndFlush(user);

    UserCreateRequest userCreateRequest = UserCreateRequest.builder()
      .email(user.getEmail())
      .password(user.getPassword())
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userCreateRequest))
      )
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.content().string(""));

    Assertions.assertThat(userRepository.findAllByEmail("email@company.com")).hasSize(1);
  }

  @Test
  public void testCreateUserEmailValidationFails() throws Exception {
    UserCreateRequest wrongEmail = UserCreateRequest.builder()
      .email("emailcompany.com")
      .password("0123456")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(wrongEmail))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest());

    Assertions.assertThat(userRepository.findAllByEmail(wrongEmail.getEmail())).isEmpty();
  }

  @Test
  public void testCreateUserPasswordValidationFails() throws Exception {
    UserCreateRequest wrongPassword = UserCreateRequest.builder()
      .email("email@company.com")
      .password("0123456")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(wrongPassword))
      )
      .andExpect(MockMvcResultMatchers.status().isBadRequest());

    Assertions.assertThat(userRepository.findAllByEmail(wrongPassword.getEmail())).isEmpty();
  }

  @Test
  public void testPasswordIsHashed() throws Exception {
    UserCreateRequest userRequest = UserCreateRequest.builder()
      .email("email@company.com")
      .password("0123456789")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userRequest))
      )
      .andExpect(MockMvcResultMatchers.status().isOk());

    User createdUser = userRepository.findByEmail(userRequest.getEmail());
    Assertions.assertThat(passwordEncoder.matches(userRequest.getPassword(), createdUser.getPassword())).isTrue();
  }
}
