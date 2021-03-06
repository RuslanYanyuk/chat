package com.chat.controllers;

import com.chat.models.ErrorResponse;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

/**
 * @author Ruslan Yaniuk
 * @date May 2017
 */
@ControllerAdvice
public class ExceptionController {

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/topic/errors")
    public ErrorResponse handleAccessDeniedException(Exception e) {
        return new ErrorResponse(e.getMessage());
    }
}
