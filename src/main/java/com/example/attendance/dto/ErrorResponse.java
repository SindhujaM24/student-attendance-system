package com.example.attendance.dto;

import java.util.Map;

/** Every error returned by the API has this shape, so the frontend can show "message" directly. */
public record ErrorResponse(String message, Map<String, String> errors) {
}
