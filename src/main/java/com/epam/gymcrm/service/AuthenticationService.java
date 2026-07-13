package com.epam.gymcrm.service;

import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.security.Credentials;
import com.epam.gymcrm.util.DtoValidator;
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
    private final DtoValidator dtoValidator;

    public boolean authenticate(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        return authenticate(credentials.username(), credentials.password(), AuthenticationTarget.USER);
    }

    public boolean authenticateTrainee(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        boolean authenticated = authenticate(
                credentials.username(), credentials.password(), AuthenticationTarget.TRAINEE);
        log.info("Trainee password verification for username={}: {}",
                credentials.username(), authenticated ? "success" : "failed");
        return authenticated;
    }

    public boolean authenticateTrainer(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        boolean authenticated = authenticate(
                credentials.username(), credentials.password(), AuthenticationTarget.TRAINER);
        log.info("Trainer password verification for username={}: {}",
                credentials.username(), authenticated ? "success" : "failed");
        return authenticated;
    }

    public void requireAuthenticated(Credentials credentials) {
        dtoValidator.validate(credentials);
        if (!authenticate(credentials.username(), credentials.password(), AuthenticationTarget.USER)) {
            throw new AuthenticationException(
                    "Invalid credentials for username: " + credentials.username());
        }
        log.debug("Authentication succeeded for username={}", credentials.username());
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
