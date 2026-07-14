package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsername(String username) {
        return getSession()
                .createQuery("FROM UserEntity u WHERE u.username = :username", UserEntity.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }

    @Transactional
    public UserEntity save(UserEntity user) {
        Session session = getSession();
        UserEntity persisted = session.merge(user);
        session.flush();
        return persisted;
    }
}
