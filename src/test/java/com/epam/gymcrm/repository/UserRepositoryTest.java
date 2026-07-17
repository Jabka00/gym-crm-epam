package com.epam.gymcrm.repository;

import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.support.MySqlIntegrationTest;
import com.epam.gymcrm.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MySqlIntegrationTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindSeedUserByUsername() {
        assertThat(userRepository.findByUsername("Alice.Walker"))
                .get()
                .extracting(UserEntity::getUsername, UserEntity::getFirstName, UserEntity::isActive)
                .containsExactly("Alice.Walker", "Alice", true);
    }

    @Test
    void shouldReturnEmptyWhenUsernameMissing() {
        assertThat(userRepository.findByUsername("No.Such.User")).isEmpty();
    }

    @Test
    void shouldFindUsernamesStartingWithPrefix() {
        List<String> usernames = userRepository.findUsernamesStartingWith("John.Smith");

        assertThat(usernames).contains("John.Smith");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsernameMatchesPrefix() {
        assertThat(userRepository.findUsernamesStartingWith("Definitely.Missing.Prefix")).isEmpty();
    }

    @Test
    void shouldSaveNewUser() {
        UserEntity user = TestDataFactory.user(null, "Repo", "User", "Repo.User", "Pass1234", true);

        UserEntity saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByUsername("Repo.User"))
                .get()
                .extracting(UserEntity::getFirstName, UserEntity::getLastName, UserEntity::getPassword)
                .containsExactly("Repo", "User", "Pass1234");
    }

    @Test
    void shouldUpdateExistingUserOnSave() {
        UserEntity existing = userRepository.findByUsername("Bob.Taylor").orElseThrow();
        existing.setFirstName("Robert");

        UserEntity updated = userRepository.save(existing);

        assertThat(updated.getFirstName()).isEqualTo("Robert");
        assertThat(userRepository.findByUsername("Bob.Taylor"))
                .get()
                .extracting(UserEntity::getFirstName)
                .isEqualTo("Robert");
    }
}
