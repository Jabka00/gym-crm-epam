package com.epam.gymcrm.repository;

import com.epam.gymcrm.util.ManualTransactionSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAuthenticationRepository {

    private static final String HQL_AUTHENTICATE =
            "SELECT COUNT(e) FROM %s e "
                    + "WHERE e.username = :username AND e.password = :password AND e.active = true";

    private final SessionFactory sessionFactory;
    private final ManualTransactionSupport transactionSupport;

    public boolean authenticate(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.USER);
    }

    public boolean authenticateTrainee(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.TRAINEE);
    }

    public boolean authenticateTrainer(String username, String password) {
        return authenticate(username, password, AuthenticationTarget.TRAINER);
    }

    private boolean authenticate(String username, String password, AuthenticationTarget target) {
        return transactionSupport.inReadOnlyTransaction(() -> {
            Long count = currentSession()
                    .createQuery(target.hql(), Long.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            boolean authenticated = count > 0;
            logAuthentication(target, username, authenticated);
            return authenticated;
        });
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
        USER("UserEntity"),
        TRAINEE("TraineeEntity"),
        TRAINER("TrainerEntity");

        private final String hql;

        AuthenticationTarget(String entityName) {
            this.hql = HQL_AUTHENTICATE.formatted(entityName);
        }

        String hql() {
            return hql;
        }
    }
}
