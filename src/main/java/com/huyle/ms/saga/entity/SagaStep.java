package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStepStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.huyle.ms.saga.constant.SagaStepStatus.STARTED;

@Getter
@Setter
@Builder
public class SagaStep implements Serializable {
    private String key;
    private byte[] payloadKey;
    private byte[] payloadValue;
    private SagaStepStatus status = STARTED;

    public SagaStep(String key, byte[] payloadKey, byte[] payloadValue) {
        this.key = key;
        this.payloadKey = payloadKey;
        this.payloadValue = payloadValue;
    }
}
