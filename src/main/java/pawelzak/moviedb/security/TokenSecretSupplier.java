package pawelzak.moviedb.security;

import java.util.function.Supplier;

public interface TokenSecretSupplier extends Supplier<byte[]> {
}
