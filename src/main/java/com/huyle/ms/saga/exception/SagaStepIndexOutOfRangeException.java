package com.huyle.ms.saga.exception;

public class SagaStepIndexOutOfRangeException extends RuntimeException {
    public SagaStepIndexOutOfRangeException(String message) {
        super(message);
    }
}
