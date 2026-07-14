package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
import com.epam.gymcrm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainerRepository {

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TrainerEntity save(TrainerEntity trainer) {
        Session session = getSession();
        if (trainer.getId() == null) {
            session.persist(trainer);
        } else {
            trainer = session.merge(trainer);
        }
        session.flush();
        log.debug("Saved trainer id={}", trainer.getId());
        return trainer;
    }

    @Transactional(readOnly = true)
    public Optional<TrainerEntity> findById(Long id) {
        log.debug("findById trainer id={}", id);
        return getSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization WHERE t.id = :id",
                        TrainerEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Optional<TrainerEntity> findByUsername(String username) {
        return getSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization "
                                + "WHERE t.user.username = :username",
                        TrainerEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<TrainerEntity> findByUsernames(Collection<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return List.of();
        }
        return getSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization "
                                + "WHERE t.user.username IN :usernames",
                        TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }

    @Transactional(readOnly = true)
    public Stream<TrainerEntity> findAll() {
        var trainers = getSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization",
                        TrainerEntity.class)
                .getResultList();
        log.debug("findAll trainers, count={}", trainers.size());
        return trainers.stream();
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        Long count = getSession()
                .createQuery(
                        "SELECT COUNT(t) FROM TrainerEntity t WHERE t.user.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(readOnly = true)
    public Optional<TrainerEntity> findActiveBySpecialization(TrainingType typeName) {
        return getSession()
                .createQuery(
                        "FROM TrainerEntity tr LEFT JOIN FETCH tr.specialization "
                                + "WHERE tr.user.active = true AND tr.specialization.typeName = :typeName",
                        TrainerEntity.class)
                .setParameter("typeName", typeName)
                .setMaxResults(1)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public List<TrainerEntity> findNotAssignedToTrainee(String traineeUsername) {
        return getSession()
                .createQuery(
                        "FROM TrainerEntity tr LEFT JOIN FETCH tr.specialization "
                                + "WHERE tr.user.active = true AND tr.id NOT IN "
                                + "(SELECT t.id FROM TraineeEntity te JOIN te.trainers t "
                                + "WHERE te.user.username = :username)",
                        TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }
}
