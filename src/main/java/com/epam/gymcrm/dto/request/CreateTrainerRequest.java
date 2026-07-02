package com.epam.gymcrm.dto.request;

public record CreateTrainerRequest(
        UserInfo user,
        String specialization
) {}
