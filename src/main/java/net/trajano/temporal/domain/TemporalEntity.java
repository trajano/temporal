package net.trajano.temporal.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.UUID;

import static net.trajano.temporal.domain.TemporalRepositoryImpl.NOT_SUPERSEDED;

/**
 * @param <S> type for the lookup key.
 * @param <T> a temporal type, usually {@link java.time.ZonedDateTime} or {@link java.time.LocalDate}.
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
public abstract class TemporalEntity<S extends Serializable, T extends Temporal & Comparable<? super T>> {

    /**
     * Effective on.
     */
    @Column(
      nullable = false,
      updatable = false
    )
    @NotNull
    private T effectiveOn;

    /**
     * Primary key for the table.  This is not expected to be used directly.
     */
    @Id
    @GeneratedValue
    @Column(
      length = 16
    )
    @Setter(AccessLevel.NONE)
    private UUID id;

    /**
     * UUID of the temporal object that supersedes this object.
     */
    @Column(
      length = 16,
      nullable = false
    )
    @NotNull
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
    @NotNull
    public abstract S getKey();

    public abstract void setKey(S key);

    /**
     * Ensure that the generated ID is used.
     */
    void nullifyId() {
        id = null;
    }

}
