package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.util.ManualTransactionSupport;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeRepository {

    private final SessionFactory sessionFactory;
    private final ManualTransactionSupport transactionSupport;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    public Optional<TrainingTypeEntity> findById(Long id) {
        return transactionSupport.inReadOnlyTransaction(() ->
                Optional.ofNullable(currentSession().get(TrainingTypeEntity.class, id)));
    }

    public Optional<TrainingTypeEntity> findByTypeName(String typeName) {
        return transactionSupport.inReadOnlyTransaction(() ->
                currentSession()
                        .createQuery(
                                "FROM TrainingTypeEntity t WHERE t.typeName = :typeName",
                                TrainingTypeEntity.class)
                        .setParameter("typeName", typeName)
                        .uniqueResultOptional());
    }

    public List<TrainingTypeEntity> findAll() {
        return transactionSupport.inReadOnlyTransaction(() ->
                currentSession()
                        .createQuery("FROM TrainingTypeEntity", TrainingTypeEntity.class)
                        .getResultList());
    }
}
