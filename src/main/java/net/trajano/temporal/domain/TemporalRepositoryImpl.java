package net.trajano.temporal.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.UUID;

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

    private static final String FIELD_EFFECTIVE_ON = "effectiveOn";

    private static final String FIELD_KEY = "key";

    private static final String FIELD_SUPERSEDED_BY = "supersededBy";

    @Autowired
    private EntityManager em;

    @Override
    public Optional<O> findByConstraint(
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

    @Override
    @Transactional
    public O saveChecked(O object, Class<O> resultType) {
        if (object.getId() != null) {
            throw new PersistenceException(String.format("Temporal object ID must not be set, got: %s", object.getId()));
        }
        if (em.contains(object)) {
            throw new PersistenceException(String.format("Temporal object must not be managed: %s", object));
        }
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<O> cq = cb.createQuery(resultType);

        final Root<O> entityRoot = cq.from(resultType);
        Predicate keyPredicate = cb.equal(entityRoot.get(FIELD_KEY), object.getKey());
        Predicate effectiveOnPredicate = cb.equal(entityRoot.get(FIELD_EFFECTIVE_ON), object.getEffectiveOn());
        Predicate supersededByPredicate = cb.equal(entityRoot.get(FIELD_SUPERSEDED_BY), TemporalEntity.NOT_SUPERSEDED);

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
        } catch (NoResultException e) {
            // no existing entry found.
        }

        em.persist(object);
        if (existing != null) {
            existing.setSupersededBy(object.getId());
            em.merge(existing);
        }

        return object;
    }

}
