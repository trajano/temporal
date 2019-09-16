package net.trajano.temporal.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * @param <S> type for the lookup key.
 * @param <T> a temporal type, usually {@link java.time.ZonedDateTime} or {@link java.time.LocalDate}.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
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
public abstract class TemporalEntity<S extends Serializable, T extends Temporal & Comparable<? super T>> {

    /**
     * UUID to represent not superseded.  This prevents having {@code NULL} values in index.
     */
    public static final UUID NOT_SUPERSEDED = UUID.fromString("00000000-0000-0000-0000-000000000000");

    /**
     * Effective on.
     */
    @Column(
      nullable = false,
      updatable = false
    )
    private T effectiveOn;

    /**
     * Primary key for the table.  This is not expected to be used directly.
     */
    @Id
    @GeneratedValue
    @Column(
      length = 16
    )
    private UUID id;

    /**
     * UUID of the temporal object that supersedes this object.
     */
    @Column(
      nullable = false
    )
    private UUID supersededBy = NOT_SUPERSEDED;

    /**
     * The key used to search for the temporal object.
     */
    @Basic(
      optional = false
    )
    @Column(
      nullable = false,
      updatable = false
    )
    public abstract S getKey();

}