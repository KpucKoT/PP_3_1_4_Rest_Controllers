package ru.kata.spring.boot_security.demo.exception_handling;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UserGlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<UserIncorrectData> handleException(NoSuchUserException e) {
        UserIncorrectData userIncorrectData = new UserIncorrectData();
        userIncorrectData.setMessage(e.getMessage());
        return new ResponseEntity<>(userIncorrectData, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<UserIncorrectData> handleException(Exception e) {
        UserIncorrectData userIncorrectData = new UserIncorrectData();
        userIncorrectData.setMessage(e.getMessage());
        return new ResponseEntity<>(userIncorrectData, HttpStatus.BAD_REQUEST);
    }
}
