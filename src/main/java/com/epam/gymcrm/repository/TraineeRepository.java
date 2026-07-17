package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.TraineeEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TraineeRepository {

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional
    public TraineeEntity save(TraineeEntity trainee) {
        Session session = getSession();
        if (trainee.getId() == null) {
            session.persist(trainee);
        } else {
            trainee = session.merge(trainee);
        }
        session.flush();
        log.debug("Trainee saved");
        return trainee;
    }

    @Transactional
    public void deleteByUsername(String username) {
        findByUsername(username).ifPresent(this::delete);
    }

    private void delete(TraineeEntity trainee) {
        trainee.getTrainers().clear();
        getSession().remove(trainee);
        log.debug("Trainee deleted");
    }

    @Transactional(readOnly = true)
    public Optional<TraineeEntity> findById(Long id) {
        return getSession()
                .createQuery(
                        "FROM TraineeEntity t LEFT JOIN FETCH t.trainers WHERE t.id = :id",
                        TraineeEntity.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    @Transactional(readOnly = true)
    public Optional<TraineeEntity> findByUsername(String username) {
        return getSession()
                .createQuery(
                        "FROM TraineeEntity t LEFT JOIN FETCH t.trainers WHERE t.user.username = :username",
                        TraineeEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}
