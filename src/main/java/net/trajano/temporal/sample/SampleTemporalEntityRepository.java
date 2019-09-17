package net.trajano.temporal.sample;

import net.trajano.temporal.domain.TemporalRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SampleTemporalEntityRepository
  extends
  CrudRepository<SampleTemporalEntity, UUID>,
  TemporalRepository<String, LocalDate, SampleTemporalEntity> {

    default Optional<SampleTemporalEntity> findByKey(String key) {
        return findByKeyAt(key, LocalDate.now());
    }

    default Optional<SampleTemporalEntity> findByKeyAt(String key, LocalDate at) {
        return findByKeyAt(key, at, SampleTemporalEntity.class);
    }

}
