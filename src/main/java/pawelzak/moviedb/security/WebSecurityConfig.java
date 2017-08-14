package pawelzak.moviedb.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Value("${token.secretLength}")
  Integer secretLength;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Autowired
  UserDetailsService userDetailsService;

  @Autowired
  TokenService tokenService;

  @Autowired
  ObjectMapper objectMapper;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
      .csrf().disable()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
      .authorizeRequests()
      .antMatchers(HttpMethod.POST, "/user").permitAll()
      .anyRequest().authenticated().and()
      .addFilter(new JwtAuthenticationFilter(tokenService, authenticationManager(), objectMapper))
      .addFilter(new JwtAuthorizationFilter(tokenService, authenticationManager()));
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public TokenSecretSupplier tokenSecretSupplier() {
    SecureRandom random = new SecureRandom();
    byte[] sharedSecret = new byte[secretLength];
    random.nextBytes(sharedSecret);

    return () -> sharedSecret;
  }
}
