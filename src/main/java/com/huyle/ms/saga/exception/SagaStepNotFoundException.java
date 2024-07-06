package com.huyle.ms.saga.exception;

public class SagaStepNotFoundException extends RuntimeException {
    public SagaStepNotFoundException(String message) {
        super(message);
    }
}
