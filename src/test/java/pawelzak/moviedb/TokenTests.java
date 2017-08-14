package pawelzak.moviedb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pawelzak.moviedb.security.TokenService;

import static pawelzak.moviedb.security.TokenService.AUTHORIZATION_HEADER_KEY;
import static pawelzak.moviedb.security.TokenService.BEARER_PREFIX;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TokenTests {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  TokenService tokenService;

  @Test
  public void testMissingTokenResponse() throws Exception {
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  public void testTokenInvalidSignatureResponse() throws Exception {
    String token = tokenService.createToken("email@company.com");
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + token + "!")
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  public void testExpiredTokenResponse() throws Exception {
    String token = tokenService.createToken("email@company.com", -1000);
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + token)
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  public void testNotATokenResponse() throws Exception {
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + "aaa.aaa.aaa")
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + "aaaaaaaaaaaa")
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  public void testTokenNoBearerResponse() throws Exception {
    String token = tokenService.createToken("email@company.com");
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, token)
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }

  @Test
  public void testNoSubjectInTokenResponse() throws Exception {
    String token = tokenService.createToken("");
    mockMvc
      .perform(MockMvcRequestBuilders
        .delete("/user")
        .header(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + token)
      )
      .andExpect(MockMvcResultMatchers.status().isUnauthorized());
  }
}
