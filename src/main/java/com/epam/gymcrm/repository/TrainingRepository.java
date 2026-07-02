package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingRepository {

    private static final String FETCH_TRAINING =
            "FROM TrainingEntity t "
                    + "LEFT JOIN FETCH t.trainee "
                    + "LEFT JOIN FETCH t.trainer "
                    + "LEFT JOIN FETCH t.trainingType";

    private final SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TrainingEntity save(TrainingEntity training) {
        Session session = currentSession();
        if (training.getId() == null) {
            session.persist(training);
        } else {
            training = session.merge(training);
        }
        session.flush();
        log.debug("Saved training id={}", training.getId());
        return training;
    }

    @Transactional
    public void delete(Long id) {
        TrainingEntity training = currentSession().get(TrainingEntity.class, id);
        if (training != null) {
            currentSession().remove(training);
            log.debug("Deleted training id={}", id);
        }
    }

    @Transactional(readOnly = true)
    public Optional<TrainingEntity> findById(Long id) {
        log.debug("findById training id={}", id);
        return currentSession()
                .createQuery(FETCH_TRAINING + " WHERE t.id = :id", TrainingEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Stream<TrainingEntity> findAll() {
        var trainings = currentSession()
                .createQuery(FETCH_TRAINING, TrainingEntity.class)
                .getResultList();
        log.debug("findAll trainings, count={}", trainings.size());
        return trainings.stream();
    }

    @Transactional(readOnly = true)
    public List<TrainingEntity> findByTraineeUsernameAndCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            Long trainingTypeId) {

        StringBuilder hql = new StringBuilder(FETCH_TRAINING + " WHERE t.trainee.username = :traineeUsername");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (trainerUsername != null && !trainerUsername.isBlank()) {
            hql.append(" AND t.trainer.username = :trainerUsername");
        }
        if (trainingTypeId != null) {
            hql.append(" AND t.trainingType.id = :trainingTypeId");
        }

        var query = currentSession()
                .createQuery(hql.toString(), TrainingEntity.class)
                .setParameter("traineeUsername", traineeUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (trainerUsername != null && !trainerUsername.isBlank()) {
            query.setParameter("trainerUsername", trainerUsername);
        }
        if (trainingTypeId != null) {
            query.setParameter("trainingTypeId", trainingTypeId);
        }

        List<TrainingEntity> trainings = query.getResultList();
        log.debug("findByTraineeUsernameAndCriteria trainee={}, count={}", traineeUsername, trainings.size());
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingEntity> findByTrainerUsernameAndCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        StringBuilder hql = new StringBuilder(FETCH_TRAINING + " WHERE t.trainer.username = :trainerUsername");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (traineeUsername != null && !traineeUsername.isBlank()) {
            hql.append(" AND t.trainee.username = :traineeUsername");
        }

        var query = currentSession()
                .createQuery(hql.toString(), TrainingEntity.class)
                .setParameter("trainerUsername", trainerUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }
        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }
        if (traineeUsername != null && !traineeUsername.isBlank()) {
            query.setParameter("traineeUsername", traineeUsername);
        }

        List<TrainingEntity> trainings = query.getResultList();
        log.debug("findByTrainerUsernameAndCriteria trainer={}, count={}", trainerUsername, trainings.size());
        return trainings;
    }
}
