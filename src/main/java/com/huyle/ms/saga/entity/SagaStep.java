package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStepStatus;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
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
    private byte[] compensationPayloadKey = null;
    private byte[] compensationPayloadValue = null;
    private String kafkaTopic;
    private String compensationKafkaTopic;
    private SagaStepStatus status = STARTED;
    private boolean hasCompensation;

    public SagaStep(String key, Object payloadKey, Object payloadValue, String kafkaTopic, boolean hasCompensation) {
        this.key = key;
        this.kafkaTopic = kafkaTopic;
        this.hasCompensation = hasCompensation;
        this.compensationKafkaTopic = kafkaTopic.concat("-compensation");
        initPayloadSerialization(payloadKey, payloadValue);
    }

    private void initPayloadSerialization(Object payloadKey, Object payloadValue) {
        KafkaAvroSerializer kafkaAvroSerializer = new KafkaAvroSerializer();
        this.payloadKey = kafkaAvroSerializer.serialize(this.kafkaTopic, payloadKey);
        this.payloadValue = kafkaAvroSerializer.serialize(this.kafkaTopic, payloadValue);
        this.compensationPayloadKey = kafkaAvroSerializer.serialize(this.compensationKafkaTopic, payloadKey);
        this.compensationPayloadValue = kafkaAvroSerializer.serialize(this.compensationKafkaTopic, payloadValue);
    }
}
