package pawelzak.moviedb.movies;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchedQuery {
  private boolean watched;
}
