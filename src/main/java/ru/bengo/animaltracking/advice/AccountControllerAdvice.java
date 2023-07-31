package ru.bengo.animaltracking.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.bengo.animaltracking.exception.AccountNotFoundException;
import ru.bengo.animaltracking.exception.NoAccessException;
import ru.bengo.animaltracking.exception.UserAlreadyExistException;

@RestControllerAdvice
public class AccountControllerAdvice {

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<?> onUserAlreadyExistException(UserAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(NoAccessException.class)
    public ResponseEntity<?> onNoAccessException(NoAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> onAccountNotFoundException(AccountNotFoundException e) {
        if (e.getMessage().startsWith("GET")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
