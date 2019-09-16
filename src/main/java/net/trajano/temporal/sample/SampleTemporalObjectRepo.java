package net.trajano.temporal.sample;

import net.trajano.temporal.domain.TemporalRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SampleTemporalObjectRepo
  extends
  CrudRepository<SampleTemporalEntity, UUID>,
  TemporalRepository<String, LocalDate, SampleTemporalEntity> {

}
