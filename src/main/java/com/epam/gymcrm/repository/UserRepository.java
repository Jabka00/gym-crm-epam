package com.epam.gymcrm.repository;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        Long count = getSession()
                .createQuery(
                        "SELECT COUNT(u) FROM UserEntity u WHERE u.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count > 0;
    }
}
