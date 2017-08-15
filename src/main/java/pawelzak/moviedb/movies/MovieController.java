package pawelzak.moviedb.movies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pawelzak.moviedb.entities.Movie;
import pawelzak.moviedb.entities.User;
import pawelzak.moviedb.user.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

  @Autowired
  UserRepository userRepository;

  @Autowired
  MovieRepository movieRepository;

  private User getUser(Authentication authorization) {
    String email = (String) authorization.getPrincipal();
    return userRepository.findByEmail(email);
  }

  private boolean userHasMovie(String movieId, Authentication authorization) {
    User user = getUser(authorization);
    Movie movie = movieRepository.findOne(movieId);
    return user != null && movie != null && user.getId().equals(movie.getUser().getId());
  }

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> create(@RequestBody MovieRequest movieRequest, Authentication authorization) {
    User user = getUser(authorization);
    if (user != null) {
      Movie movie = Movie.builder()
        .user(user)
        .title(movieRequest.getTitle())
        .description(movieRequest.getDescription())
        .watched(movieRequest.isWatched())
        .build();

      movieRepository.saveAndFlush(movie);

      return new ResponseEntity<>(movie, HttpStatus.CREATED);
    }

    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<?> listAll(@RequestBody(required = false) WatchedQuery watchedQuery, Authentication authorization) {
    User user = getUser(authorization);
    List<Movie> movies = null;
    if (watchedQuery != null) {
      movies = movieRepository.findAllByUserIdAndWatched(user.getId(), watchedQuery.isWatched());
    } else {
      movies = movieRepository.findAllByUserId(user.getId());
    }
    return new ResponseEntity<>(movies, HttpStatus.OK);
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
  @ResponseBody
  public ResponseEntity<?> delete(@PathVariable String id, Authentication authorization) {
    if (userHasMovie(id, authorization)) {
      movieRepository.delete(id);
      return new ResponseEntity(HttpStatus.OK);
    }

    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
  }

  @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
  @ResponseBody
  public ResponseEntity<?> replace(@PathVariable String id, @RequestBody MovieRequest movieRequest, Authentication authorization) {
    if (userHasMovie(id, authorization)) {
      User user = getUser(authorization);
      Movie updatedMovie = movieRepository.save(
        Movie.builder()
        .id(id)
        .user(user)
        .title(movieRequest.getTitle())
        .description(movieRequest.getDescription())
        .watched(movieRequest.isWatched())
        .build()
      );
      return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
    }

    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
  }
}
