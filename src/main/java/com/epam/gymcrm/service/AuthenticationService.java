package com.epam.gymcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final SessionFactory sessionFactory;

    public boolean matchesTraineeCredentials(String username, String password) {
        boolean matched = matches(username, password, AuthenticationTarget.TRAINEE);
        log.debug("Trainee credentials check: {}", matched ? "match" : "no match");
        return matched;
    }

    public boolean matchesTrainerCredentials(String username, String password) {
        boolean matched = matches(username, password, AuthenticationTarget.TRAINER);
        log.debug("Trainer credentials check: {}", matched ? "match" : "no match");
        return matched;
    }

    private boolean matches(String username, String password, AuthenticationTarget target) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        Long count = getSession()
                .createQuery(target.hql(), Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();
        return count > 0;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    private enum AuthenticationTarget {
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
