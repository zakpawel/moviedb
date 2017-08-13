package pawelzak.moviedb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface UserRepository extends JpaRepository<User, String> {
  User findByEmail(String email);
  List<User> findAllByEmail(String email);
}
