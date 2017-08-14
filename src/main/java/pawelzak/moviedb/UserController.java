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

  @Autowired
  TokenService tokenService;

  @RequestMapping(path = "user", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> createAccount(@Valid @RequestBody UserCreateRequest userCreateRequest) {
    User user = userRepository.findByEmail(userCreateRequest.getEmail());

    if (user == null) {
      User createdUser = userRepository.saveAndFlush(
        User.builder()
        .email(userCreateRequest.getEmail())
        .password(passwordEncoder.encode(userCreateRequest.getPassword()))
        .build());

      return new ResponseEntity(HttpStatus.OK);
    }

    return new ResponseEntity(HttpStatus.CONFLICT);
  }
}
