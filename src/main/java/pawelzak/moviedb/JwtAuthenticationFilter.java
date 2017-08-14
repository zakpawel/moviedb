package pawelzak.moviedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private TokenService tokenService;
  private AuthenticationManager authenticationManager;
  private ObjectMapper objectMapper;

  public JwtAuthenticationFilter(TokenService tokenService, AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
    this.tokenService = tokenService;
    this.authenticationManager = authenticationManager;
    this.objectMapper = objectMapper;
    setAuthenticationManager(authenticationManager);
  }
  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      UserLoginRequest user = objectMapper.readValue(request.getInputStream(), UserLoginRequest.class);
      return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
    tokenService.setToken(response, tokenService.createToken(authResult.getName()));
  }
}
