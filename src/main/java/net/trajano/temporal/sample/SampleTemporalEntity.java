package net.trajano.temporal.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.trajano.temporal.domain.TemporalEntity;

import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class SampleTemporalEntity extends TemporalEntity<String, LocalDate> {

    private String key;

    private String property;

}
