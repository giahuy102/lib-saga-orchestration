package com.huyle.ms.saga.service;

import com.huyle.ms.saga.entity.SagaStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {
    private final KafkaTemplate<byte[], byte[]> sagaTemplate;

    public void publishSagaStepEvent(UUID instanceId, byte[] key, byte[] value, String stepKey, String topic) {
        sagaTemplate.send(topic, key, value)
                .addCallback(new ListenableFutureCallback<SendResult<byte[], byte[]>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("Saga instanceId=\"{}\" - Unable to send step=\"{}\" to topic=\"{}\" due to : {}", instanceId, stepKey, topic, ex.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<byte[], byte[]> result) {
                        log.info("Saga instanceId=\"{}\" - Sent event for step=\"{}\" to topic=\"{}\" with offset=[\"{}\"]", instanceId, stepKey, topic, result.getRecordMetadata().offset());
                    }
                });
    }
}
