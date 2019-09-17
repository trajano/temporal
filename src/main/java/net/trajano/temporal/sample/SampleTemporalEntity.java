package net.trajano.temporal.sample;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.trajano.temporal.domain.TemporalEntity;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * This is a sample temporal entity with a single property and an element collection. {@link Table#uniqueConstraints()}
 * need to be specified here as it cannot be inherited from {@link MappedSuperclass}.  If {@link Data} is used, then
 * {@link EqualsAndHashCode#callSuper()} should be set to {@code true}.
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(
  uniqueConstraints = {
    @UniqueConstraint(
      columnNames = {
        "key",
        "effectiveOn",
        "supersededBy"
      }
    )
  }
)
public class SampleTemporalEntity extends TemporalEntity<String, LocalDate> {

    /**
     * The lookup key.  This specifies an {@link AttributeOverride} so that the length can be specified, note that the
     * {@link Column#updatable()}{@code = false} and {@link Column#updatable()}{@code = false} should also be set as it
     * is not inherited.
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
