package net.trajano.temporal.domain;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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

    @Autowired
    private EntityManager em;

    public Optional<O> findByConstraint(S key, T at, UUID supersededBy, Class<O> resultType) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<O> cq = cb.createQuery(resultType);

        final Root<O> entityRoot = cq.from(resultType);

        final Path<T> effectiveOn = entityRoot.get("effectiveOn");
        Predicate keyPredicate = cb.equal(entityRoot.get("key"), key);
        Predicate supersededByPredicate = cb.equal(entityRoot.get("supersededBy"), supersededBy);

        final Subquery<Comparable> effectiveOnQuery = cq.subquery(Comparable.class);
        final Root<O> effectiveOnRoot = effectiveOnQuery.from(resultType);
        Predicate keyPredicateQ = cb.equal(effectiveOnRoot.get("key"), key);
        Predicate supersededByPredicateQ = cb.equal(effectiveOnRoot.get("supersededBy"), supersededBy);
        final Path<Comparable> effectiveOnQueryPath = effectiveOnRoot.get("effectiveOn");

        final Predicate atPredicate = cb.lessThanOrEqualTo(effectiveOnRoot.get("effectiveOn"), at);

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

}
