package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainerRepository {

    private final SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TrainerEntity save(TrainerEntity trainer) {
        Session session = currentSession();
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
        return currentSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization WHERE t.id = :id",
                        TrainerEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Optional<TrainerEntity> findByUsername(String username) {
        return currentSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization WHERE t.username = :username",
                        TrainerEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Stream<TrainerEntity> findAll() {
        var trainers = currentSession()
                .createQuery(
                        "FROM TrainerEntity t LEFT JOIN FETCH t.specialization LEFT JOIN FETCH t.trainees",
                        TrainerEntity.class)
                .getResultList();
        log.debug("findAll trainers, count={}", trainers.size());
        return trainers.stream();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        Long count = currentSession()
                .createQuery("SELECT COUNT(t) FROM TrainerEntity t WHERE t.id = :id", Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        Long count = currentSession()
                .createQuery("SELECT COUNT(t) FROM TrainerEntity t WHERE t.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }

    @Transactional(readOnly = true)
    public List<TrainerEntity> findNotAssignedToTrainee(String traineeUsername) {
        return currentSession()
                .createQuery(
                        "FROM TrainerEntity tr LEFT JOIN FETCH tr.specialization "
                                + "WHERE tr.active = true AND tr.id NOT IN "
                                + "(SELECT t.id FROM TraineeEntity te JOIN te.trainers t WHERE te.username = :username)",
                        TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultList();
    }
}
