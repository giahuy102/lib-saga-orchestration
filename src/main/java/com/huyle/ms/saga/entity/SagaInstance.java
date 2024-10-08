package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStatus;
import com.huyle.ms.saga.entity.converter.ListAttributeConverter;
import com.huyle.ms.saga.exception.NoSagaStepException;
import com.huyle.ms.saga.exception.SagaStepIndexOutOfRangeException;
import com.huyle.ms.saga.exception.SagaStepNotFoundException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.huyle.ms.saga.constant.SagaStatus.IN_PROGRESS;

@Entity
@NoArgsConstructor
@Table(name = "saga_instance")
@Getter
@Setter
public class SagaInstance {
    @Id
    private UUID id;

    @Column(name = "current_step")
    private String currentStep;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    // useful to tell apart different kinds of Sagas supported by one system
    private String type;

    @Convert(converter = ListAttributeConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<SagaStep> steps = new ArrayList<>();

    public static StepBuilder stepBuilder() {
        return new StepBuilder();
    }

    public static class StepBuilder {
        private final List<SagaStep> sagaSteps = new ArrayList<>();

        public StepBuilder() {}

        public StepBuilder step(SagaStep sagaStep) {
            sagaSteps.add(sagaStep);
            return this;
        }

        public List<SagaStep> build() {
            return sagaSteps;
        }
    }

    public SagaInstance(UUID instanceId, String sagaType, List<SagaStep> sagaSteps) {
        if (sagaSteps.isEmpty()) {
            throw new NoSagaStepException("Saga instance must have at lease one step");
        }
        this.id = instanceId;
        this.currentStep = sagaSteps.get(0).getKey();
        this.status = IN_PROGRESS;
        this.type = sagaType;
        this.steps = sagaSteps;
    }

    public SagaStep getNextStep(String stepKey) {
        int index = 0;
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getKey().equals(stepKey)) {
                index = i;
                break;
            }
        }
        try {
            return steps.get(index + 1);
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeException("No more saga step found");
        }
    }

    public SagaStep getFirstStep() {
        return steps.get(0);
    }

    public int getStepsAmount() {
        return steps.size();
    }

    public SagaStep getStepAtOrder(int index) {
        try {
            return steps.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new SagaStepIndexOutOfRangeException(String.format("Saga instance %s has no step at index %d", this.id, index));
        }
    }

    public int getStepOrderIndex(String stepKey) {
        SagaStep curStep;
        for (int i = 0; i < steps.size(); ++i) {
            curStep = steps.get(i);
            if (curStep.getKey().equals(stepKey)) return i;
        }
        throw new SagaStepNotFoundException(String.format("Saga step with key %s is not found", stepKey));
    }
}
