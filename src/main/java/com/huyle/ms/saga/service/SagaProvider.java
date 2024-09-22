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
    private final KafkaService kafkaService;

    private SagaInstance findInstanceById(UUID id) {
        return sagaRepository.findById(id).orElseThrow(() -> new RuntimeException(String.format("Can't find saga instance with id %s", id)));
    }

    @Transactional
    public void initSagaInstance(UUID instanceId, String sagaType, List<SagaStep> sagaSteps) {
        log.info("Init saga instance ID=\"{}\"", instanceId);
        SagaInstance instance = new SagaInstance(instanceId, sagaType, sagaSteps);
        SagaStep firstStep = instance.getFirstStep();
        firstStep.setStatus(SagaStepStatus.STARTED);
        sagaRepository.save(instance);
        kafkaService.publishSagaStepEvent(instanceId, firstStep.getPayloadKey(), firstStep.getPayloadValue(), firstStep.getKey(), firstStep.getKafkaTopic());
    }

    @Transactional
    public void onStepEvent(String stepKey, SagaStepStatus stepStatus, UUID instanceId) {
        log.info("Receive event for step=\"{}\"", stepKey);
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
            kafkaService.publishSagaStepEvent(sagaInstance.getId(), nextStep.getPayloadKey(), nextStep.getPayloadValue(), nextStep.getKey(), nextStep.getKafkaTopic());
            return true;
        } catch (SagaStepIndexOutOfRangeException e) {
            return false;
        }
    }

    private boolean compensate(SagaInstance sagaInstance, int stepIndex) {
        try {
            SagaStep prevStep = sagaInstance.getStepAtOrder(stepIndex - 1);
            prevStep.setStatus(SagaStepStatus.COMPENSATING);
            kafkaService.publishSagaStepEvent(sagaInstance.getId(), prevStep.getPayloadKey(), prevStep.getPayloadValue(), prevStep.getKey(), prevStep.getKafkaTopic());
            return true;
        } catch (SagaStepIndexOutOfRangeException e) {
            return false;
        }
    }
}
