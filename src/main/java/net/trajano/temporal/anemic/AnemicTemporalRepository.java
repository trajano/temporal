package net.trajano.temporal.anemic;

import net.trajano.temporal.domain.TemporalRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface AnemicTemporalRepository
  extends
  TemporalRepository<String, Instant, AnemicTemporal> {

    default Optional<AnemicTemporal> findByKey(String key) {
        return findByKeyAt(key, Instant.now(), AnemicTemporal.class);
    }

    default Optional<AnemicTemporal> findByKeyAt(String key, Instant at) {
        return findByKeyAt(key, at, AnemicTemporal.class);
    }

}
