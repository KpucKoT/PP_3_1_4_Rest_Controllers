package ru.kata.spring.boot_security.demo.exception_handling;

public class UserIncorrectData {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
