package net.trajano.temporal.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

@Slf4j
class TemporalRepositoryImpl<
  S extends Serializable,
  T extends Temporal & Comparable<? super T>,
  O extends TemporalEntity<S, T>
  >
  implements TemporalRepository<S, T, O> {

    /**
     * UUID to represent not superseded temporarily.  This prevents constraint violations.
     */
    private static final UUID SUPERSEDED_TEMPORARILY = UUID.fromString("00000000-0000-0000-0000-000000000001");

    /**
     * UUID to represent not superseded.  This prevents having {@code NULL} values in index.
     */
    static final UUID NOT_SUPERSEDED = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final String FIELD_EFFECTIVE_ON = "effectiveOn";

    private static final String FIELD_KEY = "key";

    private static final String FIELD_SUPERSEDED_BY = "supersededBy";

    /**
     * A cache of that maps the the temporal entity class to its temporal type.
     */
    private final Map<Class<O>, Class<T>> temporalTypeMap = new WeakHashMap<>();

    @Autowired
    private EntityManager em;

    /**
     * Finds the temporal entity for a given time that is not superseded.
     * This implements a caching mechanism to reduce the time needed to get the temporal type.
     *
     * @param key key
     * @param at at which time
     * @param resultType result type.  This is needed as
     * {@link javax.persistence.MappedSuperclass} cannot be used for JPA queries.
     * @return temporal entity
     */
    private Optional<O> findByConstraint(
      final S key,
      final T at,
      final Class<O> resultType
    ) {

        return findByConstraint(
          key,
          at,
          TemporalRepositoryImpl.NOT_SUPERSEDED,
          getTemporalType(resultType),
          resultType
        );
    }

    @Override
    @Transactional
    public Optional<O> findByKeyAt(
      final @NotNull S key,
      final @NotNull T at,
      final @NotNull Class<O> resultType) {

        return findByConstraint(key, at, resultType);
    }

    /**
     * Gets the temporal type from the result type.  Uses the cached value if available.
     *
     * @param resultType result type
     * @return temporal type.
     */
    @SuppressWarnings("unchecked")
    private Class<T> getTemporalType(Class<O> resultType) {
        return temporalTypeMap.computeIfAbsent(
          resultType,
          t -> {
              try {
                  return (Class<T>) resultType.getMethod("getEffectiveOn").getReturnType();
              } catch (ReflectiveOperationException e) {
                  throw new IllegalStateException(e);
              }
          }
        );
    }

    /**
     * Finds the temporal entity for a given time.  This uses the computed temporal type.
     *
     * @param key key
     * @param at at which time
     * @param supersededBy specific superseded value
     * @param temporalType temporal type
     * @param resultType result type
     * @return temporal entity
     */
    private Optional<O> findByConstraint(
      final S key,
      final T at,
      final UUID supersededBy,
      final Class<T> temporalType,
      final Class<O> resultType) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<O> cq = cb.createQuery(resultType);

        final Root<O> entityRoot = cq.from(resultType);

        final Path<T> effectiveOn = entityRoot.get(FIELD_EFFECTIVE_ON);
        Predicate keyPredicate = cb.equal(entityRoot.get(FIELD_KEY), key);
        Predicate supersededByPredicate = cb.equal(entityRoot.get(FIELD_SUPERSEDED_BY), supersededBy);

        final Subquery<T> effectiveOnQuery = cq.subquery(temporalType);
        final Root<O> effectiveOnRoot = effectiveOnQuery.from(resultType);
        Predicate keyPredicateQ = cb.equal(effectiveOnRoot.get(FIELD_KEY), key);
        Predicate supersededByPredicateQ = cb.equal(effectiveOnRoot.get(FIELD_SUPERSEDED_BY), supersededBy);
        final Path<T> effectiveOnQueryPath = effectiveOnRoot.get(FIELD_EFFECTIVE_ON);

        final Predicate atPredicate = cb.lessThanOrEqualTo(effectiveOnRoot.get(FIELD_EFFECTIVE_ON), at);

        try {
            return Optional.of(
              em.createQuery(cq.select(entityRoot)
                .where(
                  keyPredicate,
                  supersededByPredicate,
                  cb.equal(
                    effectiveOn,
                    effectiveOnQuery.select(cb.greatest(effectiveOnQueryPath))
                      .where(
                        keyPredicateQ,
                        supersededByPredicateQ,
                        atPredicate
                      )
                  )
                ))
                .getSingleResult());
        } catch (final NoResultException e) {
            return Optional.empty();
        }
    }

    private O saveChecked(O object, Class<O> resultType) {
        validateObject(object);
        if (object.getId() != null) {
            log.warn(String.format("Temporal object ID should not be set, got: %s, resetting to null", object.getId()));
            object.nullifyId();
        }
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<O> cq = cb.createQuery(resultType);

        final Root<O> entityRoot = cq.from(resultType);
        Predicate keyPredicate = cb.equal(entityRoot.get(FIELD_KEY), object.getKey());
        Predicate effectiveOnPredicate = cb.equal(entityRoot.get(FIELD_EFFECTIVE_ON), object.getEffectiveOn());
        Predicate supersededByPredicate = cb.equal(entityRoot.get(FIELD_SUPERSEDED_BY), NOT_SUPERSEDED);

        O existing = null;
        try {
            existing = em.createQuery(cq.select(entityRoot)
              .where(
                keyPredicate,
                effectiveOnPredicate,
                supersededByPredicate
              )
            ).getSingleResult();
            existing.setSupersededBy(SUPERSEDED_TEMPORARILY);
            em.merge(existing);
            em.flush();
        } catch (final NoResultException e) {
            // no existing entry found.
            log.trace("No existing temporal object found");
        }

        em.persist(object);
        if (existing != null) {
            existing.setSupersededBy(object.getId());
            em.merge(existing);
        }

        return object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional
    public O saveTemporal(@Valid final O object) {
        return saveChecked(object, (Class<O>) object.getClass());
    }

    /**
     * Validates if the object is valid for saving.
     *
     * @param object object to validate.
     */
    private void validateObject(O object) {
        if (!NOT_SUPERSEDED.equals(object.getSupersededBy())) {
            throw new PersistenceException(String.format("Temporal object must not be superseded, got: %s", object.getSupersededBy()));
        }
        if (em.contains(object)) {
            throw new PersistenceException(String.format("Temporal object must not be managed: %s", object));
        }
    }

}
