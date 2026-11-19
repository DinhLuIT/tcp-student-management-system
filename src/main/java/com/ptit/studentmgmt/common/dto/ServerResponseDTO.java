package com.ptit.studentmgmt.common.dto;

import java.io.Serializable;

public class ServerResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int statusCode;
    private String message;
    private boolean success;

    public ServerResponseDTO() {
    }

    public ServerResponseDTO(int statusCode, String message, boolean success) {
        this.statusCode = statusCode;
        this.message = message;
        this.success = success;
    }

    public static ServerResponseDTO ok(String message) {
        return new ServerResponseDTO(200, message, true);
    }

    public static ServerResponseDTO error(int statusCode, String message) {
        return new ServerResponseDTO(statusCode, message, false);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ServerResponseDTO{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
