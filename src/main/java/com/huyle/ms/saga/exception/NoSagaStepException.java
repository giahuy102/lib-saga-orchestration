package com.huyle.ms.saga.exception;

public class NoSagaStepException extends RuntimeException {
    public NoSagaStepException(String message) {
        super(message);
    }
}
