package net.trajano.temporal.domain;

import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.UUID;

/**
 * @param <S> type for the key
 * @param <T> type for the effectiveOn
 * @param <O> temporal entity type
 */
public interface TemporalRepository<
  S extends Serializable,
  T extends Temporal & Comparable<? super T>,
  O extends TemporalEntity<S, T>
  > extends Repository<O, UUID> {

    /**
     * Finds the temporal entity for a given time.
     *
     * @param key key
     * @param at at which time
     * @param resultType result type.  This is needed as
     * {@link javax.persistence.MappedSuperclass} cannot be used for JPA queries.
     * @return temporal entity
     */
    Optional<O> findByKeyAt(
      final S key,
      final T at,
      final Class<O> resultType);

    /**
     * Saves the temporal with the key data overridden in the object.  Note this modifies the data in object.  It is
     * expected that the object is not managed.
     *
     * @param object temporal object to save.
     * @param effectiveOn effective on
     * @return saved object
     */
    default O saveTemporal(final O object, T effectiveOn) {
        object.setEffectiveOn(effectiveOn);
        return saveTemporal(object);
    }

    /**
     * Saves the temporal with the key data overridden in the object.  Note this modifies the data in object.  It is
     * expected that the object is not managed.
     *
     * @param object temporal object to save.
     * @param key lookup key
     * @param effectiveOn effective on
     * @return saved object
     */
    default O saveTemporal(final O object, S key, T effectiveOn) {
        object.setKey(key);
        return saveTemporal(object, effectiveOn);
    }

    /**
     * Saves the temporal object. It is
     * expected that the object is not managed.
     *
     * @param object temporal object to save.
     * @return saved object
     */
    O saveTemporal(final O object);

}
