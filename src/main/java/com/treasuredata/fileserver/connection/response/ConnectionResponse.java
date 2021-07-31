package com.treasuredata.fileserver.connection.response;

import org.springframework.http.HttpStatus;

public class ConnectionResponse {

    private String clientId;
    private int status;
    private String message;

    public ConnectionResponse(String clientId, HttpStatus status, String message) {
        this.clientId = clientId;
        this.status = status.value();
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public static ConnectionResponse of(String clientId, HttpStatus status, String message) {
        return new ConnectionResponse(clientId, status, message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClientId() {
        return clientId;
    }
}
