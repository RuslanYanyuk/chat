package com.chat.models;

/**
 * @author Ruslan Yaniuk
 * @date April 2017
 */
public class Response {
    ResponseType type;

    public Response(ResponseType type) {
        this.type = type;
    }

    public ResponseType getType() {
        return type;
    }
}
