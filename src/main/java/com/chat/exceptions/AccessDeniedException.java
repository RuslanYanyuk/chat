package com.chat.exceptions;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
public class AccessDeniedException extends Exception {

    public static final String USER_NOT_ALLOWED = "User not allowed";

    public AccessDeniedException() {
        super(USER_NOT_ALLOWED);
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}
