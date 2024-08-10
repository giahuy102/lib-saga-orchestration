package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStepStatus;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static com.huyle.ms.saga.constant.SagaStepStatus.STARTED;

@Getter
@Setter
@NoArgsConstructor
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

    public SagaStep(String key, Object payloadKey, Object payloadValue, String kafkaTopic, boolean hasCompensation,
                    KafkaAvroSerializer keySerializer, KafkaAvroSerializer valueSerializer) {
        this.key = key;
        this.kafkaTopic = kafkaTopic;
        this.hasCompensation = hasCompensation;
        this.compensationKafkaTopic = kafkaTopic.concat("-compensation");
        initPayloadSerialization(payloadKey, payloadValue, keySerializer, valueSerializer);
    }

    private void initPayloadSerialization(Object payloadKey, Object payloadValue, KafkaAvroSerializer keySerializer, KafkaAvroSerializer valueSerializer) {
        this.payloadKey = keySerializer.serialize(this.kafkaTopic, payloadKey);
        this.payloadValue = valueSerializer.serialize(this.kafkaTopic, payloadValue);
        this.compensationPayloadKey = keySerializer.serialize(this.compensationKafkaTopic, payloadKey);
        this.compensationPayloadValue = valueSerializer.serialize(this.compensationKafkaTopic, payloadValue);
    }
}
