package pawelzak.moviedb.movies;

import org.springframework.data.jpa.repository.JpaRepository;
import pawelzak.moviedb.entities.Movie;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, String> {
  List<Movie> findAllByUserId(String userId);
  List<Movie> findAllByUserIdAndWatched(String userId, boolean watched);
}
