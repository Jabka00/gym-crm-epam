package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import com.epam.gymcrm.entity.TrainerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TraineeRepository {

    private final SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TraineeEntity save(TraineeEntity trainee) {
        Session session = currentSession();
        if (trainee.getId() == null) {
            session.persist(trainee);
        } else {
            trainee = session.merge(trainee);
        }
        session.flush();
        log.debug("Saved trainee id={}", trainee.getId());
        return trainee;
    }

    @Transactional
    public void delete(Long id) {
        TraineeEntity trainee = currentSession()
                .createQuery(
                        "FROM TraineeEntity t LEFT JOIN FETCH t.trainers WHERE t.id = :id",
                        TraineeEntity.class)
                .setParameter("id", id)
                .uniqueResult();

        if (trainee != null) {
            for (TrainerEntity trainer : new HashSet<>(trainee.getTrainers())) {
                trainee.getTrainers().remove(trainer);
                trainer.getTrainees().remove(trainee);
            }
            currentSession().remove(trainee);
            log.debug("Deleted trainee id={}", id);
        }
    }

    @Transactional(readOnly = true)
    public Optional<TraineeEntity> findById(Long id) {
        log.debug("findById trainee id={}", id);
        return currentSession()
                .createQuery(
                        "FROM TraineeEntity t LEFT JOIN FETCH t.trainers WHERE t.id = :id",
                        TraineeEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Optional<TraineeEntity> findByUsername(String username) {
        return currentSession()
                .createQuery(
                        "FROM TraineeEntity t LEFT JOIN FETCH t.trainers WHERE t.username = :username",
                        TraineeEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Stream<TraineeEntity> findAll() {
        var trainees = currentSession()
                .createQuery("FROM TraineeEntity t LEFT JOIN FETCH t.trainers", TraineeEntity.class)
                .getResultList();
        log.debug("findAll trainees, count={}", trainees.size());
        return trainees.stream();
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        Long count = currentSession()
                .createQuery("SELECT COUNT(t) FROM TraineeEntity t WHERE t.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }
}
