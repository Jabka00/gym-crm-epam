package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final SessionFactory sessionFactory;

    @Transactional(readOnly = true)
    public List<String> findUsernamesStartingWith(String prefix) {
        return getSession()
                .createQuery(
                        "SELECT u.username FROM UserEntity u WHERE u.username LIKE :pattern "
                                + "ORDER BY u.username",
                        String.class)
                .setParameter("pattern", prefix + "%")
                .getResultList();
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

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }
}
