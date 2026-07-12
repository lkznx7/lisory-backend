package com.lisory.backend.envios.melhorenvio.exception;

public class MelhorEnvioException extends RuntimeException {
    private final String errorCode;

    public MelhorEnvioException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MelhorEnvioException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
