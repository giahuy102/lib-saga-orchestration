package com.huyle.ms.saga.service;

import com.huyle.ms.saga.constant.SagaStatus;
import com.huyle.ms.saga.constant.SagaStepStatus;
import com.huyle.ms.saga.entity.SagaInstance;
import com.huyle.ms.saga.entity.SagaStep;
import com.huyle.ms.saga.exception.SagaStepIndexOutOfRangeException;
import com.huyle.ms.saga.repository.SagaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
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
        firstStep.setStatus(SagaStepStatus.STARTED);
        sagaRepository.save(instance);
        kafkaTemplate.send(firstStep.getKafkaTopic(), firstStep.getPayloadKey(), firstStep.getPayloadValue());
    }

    @Transactional
    public void onStepEvent(String stepKey, SagaStepStatus stepStatus, UUID instanceId) {
        SagaInstance instance = findInstanceById(instanceId);
        int curIdxStep = instance.getStepOrderIndex(stepKey);
        SagaStep curStep = instance.getStepAtOrder(curIdxStep);
        instance.setCurrentStep(curStep.getKey());
        curStep.setStatus(stepStatus);
        if (stepStatus == SagaStepStatus.SUCCEEDED) {
            boolean hasNextStep = advance(instance, curIdxStep);
            if (!hasNextStep) instance.setStatus(SagaStatus.COMPLETED);
        } else if (stepStatus == SagaStepStatus.FAILED || stepStatus == SagaStepStatus.COMPENSATED) {
            boolean hasPrevStep = compensate(instance, curIdxStep);
            if (!hasPrevStep) instance.setStatus(SagaStatus.ABORTED);
            else instance.setStatus(SagaStatus.ABORTING);
        }
    }

    private boolean advance(SagaInstance sagaInstance, int stepIndex) {
        try {
            SagaStep nextStep = sagaInstance.getStepAtOrder(stepIndex + 1);
            nextStep.setStatus(SagaStepStatus.STARTED);
            kafkaTemplate.send(nextStep.getKafkaTopic(), nextStep.getPayloadKey(), nextStep.getPayloadValue());
            return true;
        } catch (SagaStepIndexOutOfRangeException e) {
            return false;
        }
    }

    private boolean compensate(SagaInstance sagaInstance, int stepIndex) {
        try {
            SagaStep prevStep = sagaInstance.getStepAtOrder(stepIndex - 1);
            prevStep.setStatus(SagaStepStatus.COMPENSATING);
            kafkaTemplate.send(prevStep.getCompensationKafkaTopic(), prevStep.getCompensationPayloadKey(), prevStep.getCompensationPayloadValue());
            return true;
        } catch (SagaStepIndexOutOfRangeException e) {
            return false;
        }
    }
}
