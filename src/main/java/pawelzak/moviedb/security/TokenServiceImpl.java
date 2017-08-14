package pawelzak.moviedb.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class TokenServiceImpl implements TokenService {

  public static final String AUTHORIZATION_HEADER_KEY = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  @Value("${token.expiresIn}")
  Long expiresIn;

  @Autowired
  TokenSecretSupplier tokenSecretSupplier;

  @Override
  public String extractToken(String authorizationHeader) {
    if (authorizationHeader != null) {
      return removeBearerPrefix(authorizationHeader);
    }
    return null;
  }

  @Override
  public String getToken(HttpServletRequest request) {
    return extractToken(request.getHeader(AUTHORIZATION_HEADER_KEY));
  }

  @Override
  public void setToken(HttpServletResponse response, String token) {
    response.addHeader(AUTHORIZATION_HEADER_KEY, BEARER_PREFIX + token);
  }

  @Override
  public String removeBearerPrefix(String authorizationHeader) {
    return authorizationHeader.replaceAll(BEARER_PREFIX, "");
  }

  @Override
  public String createToken(String email) {
    return createToken(email, expiresIn);
  }

  @Override
  public String createToken(String email, long expiresIn) {
    return Jwts.builder()
      .setSubject(email)
      .setExpiration(new Date(new Date().getTime() + expiresIn))
      .signWith(SignatureAlgorithm.HS512, tokenSecretSupplier.get())
      .compact();
  }

  @Override
  public String getSubject(String token) {
    String subject = Jwts.parser()
      .setSigningKey(tokenSecretSupplier.get())
      .parseClaimsJws(token)
      .getBody()
      .getSubject();
    return subject;
  }
}
