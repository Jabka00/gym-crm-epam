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

    private static final String HQL_AUTHENTICATE_USER =
            "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username AND u.password = :password";

    private final SessionFactory sessionFactory;

    private Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        Long count = currentSession()
                .createQuery(HQL_AUTHENTICATE_USER, Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getSingleResult();

        boolean authenticated = count > 0;
        log.debug("Authentication for username={}: {}", username, authenticated ? "success" : "failed");
        return authenticated;
    }
}
