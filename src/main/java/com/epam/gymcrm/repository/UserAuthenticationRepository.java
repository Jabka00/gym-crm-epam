package com.epam.gymcrm.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAuthenticationRepository {

    private final SessionFactory sessionFactory;

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.USER);
    }

    @Transactional(readOnly = true)
    public boolean authenticateTrainee(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.TRAINEE);
    }

    @Transactional(readOnly = true)
    public boolean authenticateTrainer(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.TRAINER);
    }

    private boolean authenticate(String username, String password, AuthenticationTarget target) {
        Long count = currentSession()
                .createQuery(target.hql(), Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();

        boolean authenticated = count > 0;
        logAuthentication(target, username, authenticated);
        return authenticated;
    }

    private void logAuthentication(AuthenticationTarget target, String username, boolean authenticated) {
        String outcome = authenticated ? "success" : "failed";
        switch (target) {
            case USER -> log.debug("Authentication for username={}: {}", username, outcome);
            case TRAINEE -> log.debug("trainee authentication for username={}: {}", username, outcome);
            case TRAINER -> log.debug("trainer authentication for username={}: {}", username, outcome);
        }
    }

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    private enum AuthenticationTarget {
        USER("SELECT COUNT(e) FROM UserEntity e "
                + "WHERE e.username = :username AND e.password = :password AND e.active = true"),
        TRAINEE("SELECT COUNT(e) FROM TraineeEntity e "
                + "WHERE e.user.username = :username AND e.user.password = :password AND e.user.active = true"),
        TRAINER("SELECT COUNT(e) FROM TrainerEntity e "
                + "WHERE e.user.username = :username AND e.user.password = :password AND e.user.active = true");

        private final String hql;

        AuthenticationTarget(String hql) {
            this.hql = hql;
        }

        String hql() {
            return hql;
        }
    }
}
