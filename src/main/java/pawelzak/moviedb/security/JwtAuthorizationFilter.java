package pawelzak.moviedb.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Slf4j
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

    String subject = null;

    try {
      subject = tokenService.getSubject(token);

      if (subject != null) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    }
    catch (ExpiredJwtException e) {
      log.info("{} Token: {}", e.getMessage(), token);
    }
    catch (JwtException e) {
      log.warn("Exception while parsing token:", e);
    }
    finally {
      chain.doFilter(request, response);
    }
  }
}
