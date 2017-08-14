package pawelzak.moviedb.user;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserCreateRequest {
  @Email
  private String email;

  @NotNull
  @Size(min = 10, max = 40)
  private String password;
}
