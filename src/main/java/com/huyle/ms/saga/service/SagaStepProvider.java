package com.huyle.ms.saga.service;

import com.huyle.ms.saga.entity.SagaStep;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SagaStepProvider {
    @Value("${spring.kafka.properties.schema.registry.url}")
    private String avroSchemaRegistryUrl;

    public SagaStep initSagaStep(String key, Object payloadKey, Object payloadValue, String kafkaTopic, boolean hasCompensation) {
        KafkaAvroSerializer keyKafkaAvroSerializer = buildKafkaAvroSerializer(true);
        KafkaAvroSerializer valueKafkaAvroSerializer = buildKafkaAvroSerializer(false);
        return new SagaStep(key, payloadKey, payloadValue, kafkaTopic, hasCompensation, keyKafkaAvroSerializer, valueKafkaAvroSerializer);
    }

    private byte[] serializeAvro(String topic, Object value, KafkaAvroSerializer serializer) {
        return serializer.serialize(topic, value);
    }

    private Map<String, String> buildKafkaAvroSerializerProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("schema.registry.url", avroSchemaRegistryUrl);
        return props;
    }

    private KafkaAvroSerializer buildKafkaAvroSerializer(boolean isKey) {
        KafkaAvroSerializer kafkaAvroSerializer = new KafkaAvroSerializer();
        kafkaAvroSerializer.configure(buildKafkaAvroSerializerProperties(), isKey);
        return kafkaAvroSerializer;
    }
}
