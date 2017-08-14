package pawelzak.moviedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
public class UserControllerTests {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper mapper = new ObjectMapper();

  @Autowired
  UserRepository userRepository;

  @Autowired
  TokenService tokenService;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  TokenSecretSupplier tokenSecretSupplier;

  @Before
  public void setup() {
    userRepository.deleteAll();
  }

  public Matcher<String> tokenMatcher(String email) {
    return new BaseMatcher<String>() {
      @Override
      public boolean matches(Object o) {
        if (o instanceof String) {
          String token = (String) o;
          token = token.replaceAll("Bearer ", "");
          String subject = tokenService.getSubject(token);
          return email.equals(subject);
        }
        return false;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(tokenService.createToken(email));
      }
    };
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
      .andExpect(MockMvcResultMatchers.content().string(""));

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

  @Test
  public void testUserLoginSuccessfully() throws Exception {
    String password = "0123456789";

    User newUser = User.builder()
      .email("email@company.com")
      .password(passwordEncoder.encode(password))
      .build();

    User savedUser = userRepository.saveAndFlush(newUser);

    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
      .email(newUser.getEmail())
      .password(password)
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userLoginRequest))
      )
      .andExpect(MockMvcResultMatchers.header().string("Authorization", tokenMatcher(userLoginRequest.getEmail())))
      .andExpect(MockMvcResultMatchers.status().isOk());
  }

  @Test
  public void testUserLoginFailsWhenWrongPassword() throws Exception {
    String password = "0123456789";

    User newUser = User.builder()
      .email("email@company.com")
      .password(passwordEncoder.encode(password))
      .build();

    User savedUser = userRepository.saveAndFlush(newUser);

    UserLoginRequest userLoginRequest = UserLoginRequest.builder()
      .email(newUser.getEmail())
      .password(password + "!")
      .build();

    mockMvc
      .perform(MockMvcRequestBuilders
        .post("/login")
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(userLoginRequest))
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }
}
