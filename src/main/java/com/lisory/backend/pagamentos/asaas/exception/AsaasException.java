package com.lisory.backend.pagamentos.asaas.exception;

public class AsaasException extends RuntimeException {

    private final int httpStatus;
    private final String responseBody;

    public AsaasException(String message) {
        super(message);
        this.httpStatus = 0;
        this.responseBody = null;
    }

    public AsaasException(String operation, int httpStatus, String responseBody) {
        super("Asaas could not complete " + operation + " (HTTP " + httpStatus + ")");
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
