package pawelzak.moviedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

  @Test
  public void testCreateUserSuccessfully() throws Exception {
    UserCreateRequest userCreateRequest = UserCreateRequest.builder()
      .email("email@company.com")
      .password("1234567890")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userCreateRequest))
      )
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
      .andExpect(MockMvcResultMatchers.jsonPath("$.token").isString());

    assert userRepository.findAllByEmail("email@company.com").size() == 1;
  }

  @Test
  public void testCreateExistingUserFails() throws Exception {
    String email = "email@company.com";
    String password = "0123456789";

    userRepository.saveAndFlush(
      User.builder()
        .email(email)
        .password(password)
        .build());

    UserCreateRequest userCreateRequest = UserCreateRequest.builder()
      .email(email)
      .password(password)
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/user")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userCreateRequest))
      )
      .andExpect(MockMvcResultMatchers.status().isConflict())
      .andExpect(MockMvcResultMatchers.content().string(""));

    assert userRepository.findAllByEmail("email@company.com").size() == 1;
  }

}
