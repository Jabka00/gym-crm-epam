package com.epam.gymcrm.util;

import com.epam.gymcrm.entity.UserEntity;
import com.epam.gymcrm.service.UserCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializationUtil {

    private final UserCredentialService userCredentialService;

    public <T extends UserEntity> T createUser(T user, UnaryOperator<T> saver, String userType) {
        String username = userCredentialService.generateUniqueUsername(
                user.getFirstName(),
                user.getLastName()
        );
        String password = userCredentialService.generatePassword();

        user.setUsername(username);
        user.setPassword(password);
        user.setActive(true);

        T savedUser = saver.apply(user);
        log.info("{} profile created successfully with ID: {}", userType, savedUser.getId());
        return savedUser;
    }
}
