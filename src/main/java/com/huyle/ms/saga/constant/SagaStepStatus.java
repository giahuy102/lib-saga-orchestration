package com.huyle.ms.saga.constant;

public enum SagaStepStatus {
    NOT_STARTED, STARTED, SUCCEEDED, FAILED, COMPENSATING, COMPENSATED, COMPENSATION_FAILED
}
