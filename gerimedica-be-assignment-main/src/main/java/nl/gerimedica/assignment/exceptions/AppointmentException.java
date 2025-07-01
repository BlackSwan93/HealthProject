package nl.gerimedica.assignment.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AppointmentException extends RuntimeException {
    public AppointmentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AppointmentException(String message) {
        super(message);
    }
}

