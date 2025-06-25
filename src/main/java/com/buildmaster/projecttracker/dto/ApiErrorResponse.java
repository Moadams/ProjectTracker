package com.buildmaster.projecttracker.dto;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        String message,
        String data,
        LocalDateTime timestamp
) {
}
