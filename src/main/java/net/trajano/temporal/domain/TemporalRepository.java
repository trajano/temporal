package net.trajano.temporal.domain;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.UUID;

public interface TemporalRepository<
  S extends Serializable,
  T extends Temporal & Comparable<? super T>,
  O extends TemporalEntity<S, T>
  > {

    @SuppressWarnings("unchecked")
    default Optional<O> findByConstraint(S key, T at, UUID supersededBy, Class<O> resultType) {
        try {
            return findByConstraint(key, at, supersededBy, (Class<T>) resultType.getMethod("getEffectiveOn").getReturnType(), resultType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    Optional<O> findByConstraint(S key, T at, UUID supersededBy, Class<T> temporalType, Class<O> resultType);

    default Optional<O> findByKeyAt(
      final S key,
      final T at,
      final Class<O> resultType) {

        return findByConstraint(key, at, TemporalEntity.NOT_SUPERSEDED, resultType);
    }

    O saveChecked(O object, Class<O> resultType);

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
     * Saves the temporal object.  Note this modifies the data in object.  It is
     * expected that the object is not managed.
     *
     * @param object temporal object to save.
     * @return saved object
     */
    @SuppressWarnings("unchecked")
    default O saveTemporal(final O object) {
        object.setId(null);
        return saveChecked(object, (Class<O>) object.getClass());
    }

}
