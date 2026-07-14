package com.epam.gymcrm.service;

import com.epam.gymcrm.dto.Credentials;
import com.epam.gymcrm.exception.AuthenticationException;
import com.epam.gymcrm.model.AuthenticationResult;
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

    public AuthenticationResult authenticate(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        return authenticate(credentials.username(), credentials.password(), AuthenticationTarget.USER);
    }

    public AuthenticationResult authenticateTrainee(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        AuthenticationResult result = authenticate(
                credentials.username(), credentials.password(), AuthenticationTarget.TRAINEE);
        log.info("Trainee password verification: {}", result.isSuccess() ? "success" : "failed");
        return result;
    }

    public AuthenticationResult authenticateTrainer(String username, String password) {
        Credentials credentials = new Credentials(username, password);
        dtoValidator.validate(credentials);
        AuthenticationResult result = authenticate(
                credentials.username(), credentials.password(), AuthenticationTarget.TRAINER);
        log.info("Trainer password verification: {}", result.isSuccess() ? "success" : "failed");
        return result;
    }

    public void requireAuthenticated(Credentials credentials) {
        dtoValidator.validate(credentials);
        if (!authenticate(credentials.username(), credentials.password(), AuthenticationTarget.USER).isSuccess()) {
            throw new AuthenticationException("Invalid credentials");
        }
        log.debug("Authentication succeeded");
    }

    private AuthenticationResult authenticate(String username, String password, AuthenticationTarget target) {
        Long count = getSession()
                .createQuery(target.hql(), Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();

        AuthenticationResult result = AuthenticationResult.from(count > 0);
        logAuthentication(target, result);
        return result;
    }

    private void logAuthentication(AuthenticationTarget target, AuthenticationResult result) {
        String outcome = result.isSuccess() ? "success" : "failed";
        switch (target) {
            case USER -> log.debug("Authentication: {}", outcome);
            case TRAINEE -> log.debug("Trainee authentication: {}", outcome);
            case TRAINER -> log.debug("Trainer authentication: {}", outcome);
        }
    }

    private Session getSession() {
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
