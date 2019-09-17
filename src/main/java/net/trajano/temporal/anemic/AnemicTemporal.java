package net.trajano.temporal.anemic;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.trajano.temporal.domain.TemporalEntity;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a sample temporal entity with just an {@link ElementCollection} that uses {@link ZonedDateTime} as the
 * effectiveFrom. {@link Table#uniqueConstraints()} need to be specified here as it cannot be inherited from {@link
 * MappedSuperclass}.  If {@link Data} is used, then {@link EqualsAndHashCode#callSuper()} should be set to {@code
 * true}.
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
public class AnemicTemporal extends TemporalEntity<String, Instant> {

    @ElementCollection(
      fetch = FetchType.EAGER
    )
    private Map<String, String> additionalAttributes = new ConcurrentHashMap<>();

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

    public String getAdditionalAttribute(String key) {
        return additionalAttributes.get(key);
    }

    @JsonAnyGetter
    public Map<String, String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    @JsonAnySetter
    public void setAdditionalAttribute(final String name, String value) {
        additionalAttributes.put(name, value);
    }

}
