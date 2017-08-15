package pawelzak.moviedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pawelzak.moviedb.entities.Movie;
import pawelzak.moviedb.entities.User;
import pawelzak.moviedb.movies.MovieRepository;
import pawelzak.moviedb.movies.MovieRequest;
import pawelzak.moviedb.movies.WatchedQuery;
import pawelzak.moviedb.security.TokenService;
import pawelzak.moviedb.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class MovieTests {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  UserRepository userRepository;

  @Autowired
  MovieRepository movieRepository;

  @Autowired
  TokenService tokenService;

  @Autowired
  ObjectMapper mapper;

  String token;

  User user;

  @Before
  public void setup() {
    movieRepository.deleteAll();
    userRepository.deleteAll();

    user = User.builder()
      .email("email@company.com")
      .password("0123456789")
      .build();

    userRepository.save(user);

    token = tokenService.createToken(user.getEmail());
  }

  @Test
  public void testCreateMovie() throws Exception {
    MovieRequest movieRequest = MovieRequest.builder()
      .title("title")
      .description("description")
      .watched(false)
      .build();

    ResultActions result = mockMvc.perform(MockMvcRequestBuilders
        .post("/movies")
        .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .content(mapper.writeValueAsString(movieRequest))
    )
    .andExpect(MockMvcResultMatchers.status().isCreated());

    Assertions.assertThat(movieRepository.findAllByUserId(user.getId())).hasSize(1);
  }

  @Test
  public void testListAllMovies() throws Exception {
    Movie movie = Movie.builder()
      .user(user)
      .title("title")
      .description("description")
      .watched(false)
      .build();

    for (int i=0; i<5; i++) {
      movieRepository.saveAndFlush(movie);
      movie.setId(null);
      movie.setWatched((i % 2 == 0));
    }

    List<Movie> movies = movieRepository.findAllByUserId(user.getId());

    Assertions.assertThat(movies).hasSize(5);

    mockMvc.perform(MockMvcRequestBuilders
      .get("/movies")
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isOk())
    .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(movies)));

    mockMvc.perform(MockMvcRequestBuilders
      .get("/movies")
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .content(mapper.writeValueAsString(WatchedQuery.builder().watched(true).build()))
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isOk())
    .andExpect(MockMvcResultMatchers.content()
      .string(mapper.writeValueAsString(movies.stream()
        .filter(Movie::isWatched)
        .collect(Collectors.toList()))));

    mockMvc.perform(MockMvcRequestBuilders
      .get("/movies")
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .content(mapper.writeValueAsString(WatchedQuery.builder().watched(false).build()))
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isOk())
    .andExpect(MockMvcResultMatchers.content()
      .string(mapper.writeValueAsString(movies.stream()
        .filter(m -> !m.isWatched())
        .collect(Collectors.toList()))));
  }

  @Test
  public void testDeleteMovie() throws Exception {
    Movie movie = Movie.builder()
      .user(user)
      .title("title")
      .description("description")
      .watched(false)
      .build();

    movieRepository.saveAndFlush(movie);

    mockMvc.perform(MockMvcRequestBuilders
      .delete("/movies/" + movie.getId())
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isOk())
    .andExpect(MockMvcResultMatchers.content().string(""));

    Assertions.assertThat(movieRepository.exists(movie.getId())).isFalse();
  }

  @Test
  public void testUserCanOnlyDeleteOwnMovies() throws Exception {
    User otherUser = User.builder()
      .email("otherEmail@company.com")
      .password("0123456789")
      .build();

    userRepository.saveAndFlush(otherUser);

    Movie movie = Movie.builder()
      .user(otherUser)
      .title("title")
      .description("description")
      .watched(false)
      .build();

    movieRepository.saveAndFlush(movie);

    mockMvc.perform(MockMvcRequestBuilders
      .delete("/movies/" + movie.getId())
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    Assertions.assertThat(movieRepository.exists(movie.getId())).isTrue();
  }

  @Test
  public void testEditMovie() throws Exception {
    Movie movie = Movie.builder()
      .user(user)
      .title("title")
      .description("description")
      .watched(false)
      .build();

    movieRepository.saveAndFlush(movie);

    MovieRequest movieRequest = MovieRequest.builder()
      .description("newtitle")
      .build();

    ResultActions result = mockMvc.perform(MockMvcRequestBuilders
      .put("/movies/" + movie.getId())
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .content(mapper.writeValueAsString(movieRequest))
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    );

    Movie updatedMovie = movieRepository.findOne(movie.getId());

    result
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.content().string(mapper.writeValueAsString(updatedMovie)));

    Assertions.assertThat(updatedMovie).isNotNull();
    Assertions.assertThat(updatedMovie).isEqualToComparingOnlyGivenFields(movieRequest, "title", "description", "watched");
  }

  @Test
  public void testUserCanOnlyEditOwnMovies() throws Exception {
    User otherUser = User.builder()
      .email("otherEmail@company.com")
      .password("0123456789")
      .build();

    userRepository.saveAndFlush(otherUser);

    Movie movie = Movie.builder()
      .user(otherUser)
      .title("title")
      .description("description")
      .watched(false)
      .build();

    MovieRequest movieRequest = MovieRequest.builder()
      .title(movie.getTitle() + "!")
      .description(movie.getDescription())
      .watched(movie.isWatched())
      .build();

    movieRepository.saveAndFlush(movie);

    mockMvc.perform(MockMvcRequestBuilders
      .put("/movies/" + movie.getId())
      .header(TokenService.AUTHORIZATION_HEADER_KEY, TokenService.BEARER_PREFIX + token)
      .content(mapper.writeValueAsString(movieRequest))
      .contentType(MediaType.APPLICATION_JSON_UTF8)
    )
    .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    Assertions.assertThat(movieRepository.findOne(movie.getId())).isEqualTo(movie);
  }
}
