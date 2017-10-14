package com.chat.exceptions;

/**
 * @author Ruslan Yaniuk
 * @date October 2017
 */
public class MessageDeliveryException extends Exception {
    public MessageDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
