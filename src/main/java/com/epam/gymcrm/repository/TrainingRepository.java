package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TrainingEntity;
import com.epam.gymcrm.util.ManualTransactionSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

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
                    + "LEFT JOIN FETCH t.trainer tr "
                    + "LEFT JOIN FETCH tr.specialization "
                    + "LEFT JOIN FETCH t.trainingType";

    private final SessionFactory sessionFactory;
    private final ManualTransactionSupport transactionSupport;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    public TrainingEntity save(TrainingEntity training) {
        return transactionSupport.inTransaction(() -> {
            Session session = currentSession();
            TrainingEntity persisted;
            if (training.getId() == null) {
                session.persist(training);
                persisted = training;
            } else {
                persisted = session.merge(training);
            }
            session.flush();
            log.debug("Saved training id={}", persisted.getId());
            return persisted;
        });
    }

    public void delete(Long id) {
        transactionSupport.inTransaction(() -> {
            TrainingEntity training = currentSession().get(TrainingEntity.class, id);
            if (training != null) {
                currentSession().remove(training);
                log.debug("Deleted training id={}", id);
            }
        });
    }

    public Optional<TrainingEntity> findById(Long id) {
        return transactionSupport.inReadOnlyTransaction(() -> {
            log.debug("findById training id={}", id);
            return currentSession()
                    .createQuery(FETCH_TRAINING + " WHERE t.id = :id", TrainingEntity.class)
                    .setParameter("id", id)
                    .uniqueResultOptional();
        });
    }

    public Stream<TrainingEntity> findAll() {
        return transactionSupport.inReadOnlyTransaction(() -> {
            var trainings = currentSession()
                    .createQuery(FETCH_TRAINING, TrainingEntity.class)
                    .getResultList();
            log.debug("findAll trainings, count={}", trainings.size());
            return trainings.stream();
        });
    }

    public List<TrainingEntity> findByTraineeUsernameAndCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerUsername,
            String trainingTypeName) {

        return transactionSupport.inReadOnlyTransaction(() -> {
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
            if (trainingTypeName != null && !trainingTypeName.isBlank()) {
                hql.append(" AND t.trainingType.typeName = :trainingTypeName");
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
            if (trainingTypeName != null && !trainingTypeName.isBlank()) {
                query.setParameter("trainingTypeName", trainingTypeName);
            }

            List<TrainingEntity> trainings = query.getResultList();
            log.debug("findByTraineeUsernameAndCriteria trainee={}, count={}", traineeUsername, trainings.size());
            return trainings;
        });
    }

    public boolean existsByTraineeId(Long traineeId) {
        return transactionSupport.inReadOnlyTransaction(() -> {
            Long count = currentSession()
                    .createQuery(
                            "SELECT COUNT(t) FROM TrainingEntity t WHERE t.trainee.id = :traineeId",
                            Long.class)
                    .setParameter("traineeId", traineeId)
                    .getSingleResult();
            log.debug("existsByTraineeId traineeId={}, count={}", traineeId, count);
            return count > 0;
        });
    }

    public List<TrainingEntity> findByTrainerUsernameAndCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeUsername) {

        return transactionSupport.inReadOnlyTransaction(() -> {
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
        });
    }
}
