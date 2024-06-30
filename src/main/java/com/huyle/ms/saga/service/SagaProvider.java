package com.huyle.ms.saga.service;

import com.huyle.ms.saga.entity.SagaInstance;
import com.huyle.ms.saga.entity.SagaStep;
import com.huyle.ms.saga.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class SagaProvider {

    private final SagaRepository sagaRepository;
    private final KafkaTemplate<byte[], byte[]> kafkaTemplate;

    private SagaInstance findInstanceById(UUID id) {
        return sagaRepository.findById(id).orElseThrow(() -> new RuntimeException(String.format("Can't find saga instance with id %s", id)));
    }

    @Transactional
    public void initSagaInstance(String sagaType, List<SagaStep> sagaSteps) {
        SagaInstance instance = new SagaInstance(sagaType, sagaSteps);
        sagaRepository.save(instance);
        SagaStep firstStep = instance.getFirstStep();
        // TODO: Send payload to kafka
    }
}
