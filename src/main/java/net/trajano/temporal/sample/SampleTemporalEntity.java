package net.trajano.temporal.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.trajano.temporal.domain.TemporalEntity;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class SampleTemporalEntity extends TemporalEntity<String, LocalDate> {

    /**
     * The lookup key.  This specifies an {@link AttributeOverride} so that the length can be specified, note that the
     * {@code nullable = false} and {@code updatable = false} should also be set as it is not inherited.
     */
    @AttributeOverride(
      name = "key",
      column = @Column(
        length = 32,
        nullable = false,
        updatable = false
      )
    )
    private String key;

    private String property;

}
