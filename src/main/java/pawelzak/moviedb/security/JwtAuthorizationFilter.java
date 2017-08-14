package pawelzak.moviedb.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
  private TokenService tokenService;

  public JwtAuthorizationFilter(TokenService tokenService, AuthenticationManager authenticationManager) {
    super(authenticationManager);
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    String token = tokenService.getToken(request);
    if (token == null) {
      chain.doFilter(request, response);
      return;
    }

    String subject = tokenService.getSubject(token);

    if (subject != null) {
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject, null);
      SecurityContextHolder.getContext().setAuthentication(authentication);
      chain.doFilter(request, response);
    }

  }
}
