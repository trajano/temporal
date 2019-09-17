package net.trajano.temporal.anemic;

import net.trajano.temporal.domain.TemporalRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface AnemicTemporalRepository
  extends
  TemporalRepository<String, ZonedDateTime, AnemicTemporal> {

    default Optional<AnemicTemporal> findByKey(String key) {
        return findByKeyAt(key, ZonedDateTime.now(), AnemicTemporal.class);
    }

    default Optional<AnemicTemporal> findByKeyAt(String key, ZonedDateTime at) {
        return findByKeyAt(key, at, AnemicTemporal.class);
    }

}
