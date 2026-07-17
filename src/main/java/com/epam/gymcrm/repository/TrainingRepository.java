package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.model.TrainingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingRepository {

    private static final String FETCH_TRAINING =
            "FROM TrainingEntity t "
                    + "LEFT JOIN FETCH t.trainee "
                    + "LEFT JOIN FETCH t.trainer tr "
                    + "LEFT JOIN FETCH tr.specialization "
                    + "LEFT JOIN FETCH t.trainingType";

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TrainingEntity save(TrainingEntity training) {
        Session session = getSession();
        TrainingEntity persisted;
        if (training.getId() == null) {
            session.persist(training);
            persisted = training;
        } else {
            persisted = session.merge(training);
        }
        session.flush();
        log.debug("Training saved");
        return persisted;
    }

    @Transactional(readOnly = true)
    public List<TrainingEntity> findByTraineeUsernameAndCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            TrainingType trainingTypeName) {

        StringBuilder hql = new StringBuilder(
                FETCH_TRAINING + " WHERE t.trainee.user.username = :traineeUsername");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (trainerUsername != null && !trainerUsername.isBlank()) {
            hql.append(" AND t.trainer.user.username = :trainerUsername");
        }
        if (trainingTypeName != null) {
            hql.append(" AND t.trainingType.typeName = :trainingTypeName");
        }

        var query = getSession()
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
        if (trainingTypeName != null) {
            query.setParameter("trainingTypeName", trainingTypeName);
        }

        List<TrainingEntity> trainings = query.getResultList();
        log.debug("Trainee trainings fetched, count={}", trainings.size());
        return trainings;
    }

    @Transactional(readOnly = true)
    public List<TrainingEntity> findByTrainerUsernameAndCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        StringBuilder hql = new StringBuilder(
                FETCH_TRAINING + " WHERE t.trainer.user.username = :trainerUsername");

        if (fromDate != null) {
            hql.append(" AND t.trainingDate >= :fromDate");
        }
        if (toDate != null) {
            hql.append(" AND t.trainingDate <= :toDate");
        }
        if (traineeUsername != null && !traineeUsername.isBlank()) {
            hql.append(" AND t.trainee.user.username = :traineeUsername");
        }

        var query = getSession()
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
        log.debug("Trainer trainings fetched, count={}", trainings.size());
        return trainings;
    }
}
