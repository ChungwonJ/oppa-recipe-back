package org.example.global.exception;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    private String status;
    private int code;
    private String message;
    private LocalDateTime timestamp;

    public static ResponseEntity<ErrorResponse> toResponseEntity(HttpStatus status, String message) {
        return new ResponseEntity<>(
                ErrorResponse.builder()
                        .status(status.name())
                        .code(status.value())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build(),
                status
        );
    }

}