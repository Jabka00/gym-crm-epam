package com.epam.gymcrm.service;

import com.epam.gymcrm.repository.UserAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserAuthenticationRepository userAuthenticationRepository;

    public boolean authenticate(String username, String password) {
        return userAuthenticationRepository.authenticate(username, password);
    }
}
