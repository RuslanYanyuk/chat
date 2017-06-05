package com.chat.models;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
public class ErrorResponse {

    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(Throwable e) {
        message = e.getMessage();
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
