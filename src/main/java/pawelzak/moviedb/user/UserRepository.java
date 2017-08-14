package pawelzak.moviedb.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pawelzak.moviedb.entities.User;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
  User findByEmail(String email);
  List<User> findAllByEmail(String email);
  @Transactional
  void deleteByEmail(String email);
}
