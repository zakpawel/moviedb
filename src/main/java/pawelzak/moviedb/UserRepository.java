package pawelzak.moviedb;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, String> {
  User findByEmail(String email);
}
