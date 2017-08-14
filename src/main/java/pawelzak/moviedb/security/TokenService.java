package pawelzak.moviedb.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TokenService {
  public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  String getToken(HttpServletRequest request);

  void setToken(HttpServletResponse response, String token);

  String extractToken(String authorizationHeader);

  String removeBearerPrefix(String authorizationHeader);

  String createToken(String email);

  String createToken(String email, long expiresIn);

  String getSubject(String token);
}
