package pawelzak.moviedb.movies;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MovieRequest {
  private String title;
  private String description;
  private boolean watched;
}
