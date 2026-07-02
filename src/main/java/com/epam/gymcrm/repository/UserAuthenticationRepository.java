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

    private static final String HQL_AUTHENTICATE_USER =
            "SELECT COUNT(u) FROM UserEntity u "
                    + "WHERE u.username = :username AND u.password = :password AND u.active = true";

    private final SessionFactory sessionFactory;
    private final ManualTransactionSupport transactionSupport;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    public boolean authenticate(String username, String password) {
        return transactionSupport.inReadOnlyTransaction(() -> {
            Long count = currentSession()
                    .createQuery(HQL_AUTHENTICATE_USER, Long.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();

            boolean authenticated = count > 0;
            log.debug("Authentication for username={}: {}", username, authenticated ? "success" : "failed");
            return authenticated;
        });
    }
}
