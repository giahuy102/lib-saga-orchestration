package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStepStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.huyle.ms.saga.constant.SagaStepStatus.STARTED;

@Getter
@Setter
public class SagaStep implements Serializable {
    private String key;
    private byte[] payloadKey;
    private byte[] payloadValue;
    private String kafkaTopic;
    private String kafkaCompensationTopic;
    private SagaStepStatus status = STARTED;

    public SagaStep(String key, byte[] payloadKey, byte[] payloadValue, String kafkaTopic) {
        this.key = key;
        this.payloadKey = payloadKey;
        this.payloadValue = payloadValue;
        this.kafkaTopic = kafkaTopic;
        this.kafkaCompensationTopic = kafkaTopic.concat("-compensation");
    }
}
