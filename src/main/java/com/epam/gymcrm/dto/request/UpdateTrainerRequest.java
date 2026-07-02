package com.epam.gymcrm.dto.request;

public record UpdateTrainerRequest(
        Long userId,
        UserInfo user,
        String specialization,
        boolean active
) {}
