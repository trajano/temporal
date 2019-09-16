package net.trajano.temporal.domain;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.UUID;

public interface TemporalRepository<
  S extends Serializable,
  T extends Temporal & Comparable<? super T>,
  O extends TemporalEntity<S, T>
  > {

    Optional<O> findByConstraint(S key, T at, UUID supersededBy, Class<O> resultType);

    default Optional<O> findTemporally(
      final S key,
      final T at,
      final Class<O> resultType) {

        return findByConstraint(key, at, TemporalEntity.NOT_SUPERSEDED, resultType);
    }

}
