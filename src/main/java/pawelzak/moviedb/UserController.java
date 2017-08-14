package pawelzak.moviedb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Slf4j
public class UserController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder passwordEncoder;

  @RequestMapping(path = "user", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> createAccount(
    @Valid @RequestBody UserCreateRequest userCreateRequest
    ) {
    User user = userRepository.findByEmail(userCreateRequest.getEmail());

    if (user == null) {
      userRepository.saveAndFlush(
        User.builder()
        .email(userCreateRequest.getEmail())
        .password(passwordEncoder.encode(userCreateRequest.getPassword()))
        .build());

      return new ResponseEntity<TokenResponse>(
        TokenResponse.builder().token("").build(),
        HttpStatus.OK);
    }

    return new ResponseEntity(HttpStatus.CONFLICT);

  }
}
