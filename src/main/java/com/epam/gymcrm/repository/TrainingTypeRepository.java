package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingTypeEntity;
import com.epam.gymcrm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrainingTypeRepository {

    private final SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional(readOnly = true)
    public Optional<TrainingTypeEntity> findById(Long id) {
        return Optional.ofNullable(currentSession().get(TrainingTypeEntity.class, id));
    }

    @Transactional(readOnly = true)
    public Optional<TrainingTypeEntity> findByTypeName(TrainingType typeName) {
        return currentSession()
                .createQuery(
                        "FROM TrainingTypeEntity t WHERE t.typeName = :typeName",
                        TrainingTypeEntity.class)
                .setParameter("typeName", typeName)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<TrainingTypeEntity> findAll() {
        return currentSession()
                .createQuery("FROM TrainingTypeEntity", TrainingTypeEntity.class)
                .getResultList();
    }
}
