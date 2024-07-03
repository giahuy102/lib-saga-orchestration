package com.huyle.ms.saga.service;

import com.huyle.ms.saga.constant.SagaStepStatus;
import com.huyle.ms.saga.entity.SagaInstance;
import com.huyle.ms.saga.entity.SagaStep;
import com.huyle.ms.saga.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.huyle.ms.saga.constant.SagaStatus.COMPLETED;
import static com.huyle.ms.saga.constant.SagaStepStatus.FAILED;
import static com.huyle.ms.saga.constant.SagaStepStatus.STARTED;
import static com.huyle.ms.saga.constant.SagaStepStatus.SUCCEEDED;

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
        SagaStep firstStep = instance.getFirstStep();
        firstStep.setStatus(STARTED);
        sagaRepository.save(instance);
        kafkaTemplate.send(firstStep.getKafkaTopic(), firstStep.getPayloadKey(), firstStep.getPayloadValue());
    }

    public void onStepEvent(String stepKey, SagaStepStatus stepStatus, UUID instanceId) {
        SagaInstance instance = findInstanceById(instanceId);
        int curIdxStep = instance.getStepOrderIndex(stepKey);
        SagaStep curStep = instance.getStepAtOrder(curIdxStep);
        instance.setCurrentStep(curStep.getKey());
        if (stepStatus == SUCCEEDED) {
            curStep.setStatus(SUCCEEDED);
            boolean hasNextStep = advance(instance, curIdxStep);
            if (!hasNextStep) instance.setStatus(COMPLETED);
        } else if (stepStatus == FAILED) {

        }
    }

    private boolean advance(SagaInstance sagaInstance, int stepIndex) {
        try {
            SagaStep nextStep = sagaInstance.getStepAtOrder(stepIndex + 1);
            nextStep.setStatus(STARTED);
            kafkaTemplate.send(nextStep.getKafkaTopic(), nextStep.getPayloadKey(), nextStep.getPayloadValue());
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
