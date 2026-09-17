package dev.relaydesk.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(String message) {
        super(message);
    }
}
