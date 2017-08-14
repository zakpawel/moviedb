package pawelzak.moviedb;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

  @Value("${token.expiresIn}")
  Long expiresIn;

  @Autowired
  TokenSecretSupplier tokenSecretSupplier;

  public String createToken(String email) {
    return createToken(email, expiresIn);
  }

  public String createToken(String email, long expiresIn) {
    return Jwts.builder()
      .setSubject(email)
      .setExpiration(new Date(new Date().getTime() + expiresIn))
      .signWith(SignatureAlgorithm.HS512, tokenSecretSupplier.get())
      .compact();
  }

  public String getSubject(String token) {
    String subject = Jwts.parser()
      .setSigningKey(tokenSecretSupplier.get())
      .parseClaimsJws(token)
      .getBody()
      .getSubject();
    return subject;
  }
}
