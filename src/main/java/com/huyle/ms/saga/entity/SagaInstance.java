package com.huyle.ms.saga.entity;

import com.huyle.ms.saga.constant.SagaStatus;
import com.huyle.ms.saga.entity.converter.ListAttributeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.huyle.ms.saga.constant.SagaStatus.STARTED;

@Entity
@NoArgsConstructor
@Table(name = "saga_instance")
@Getter
@Setter
public class SagaInstance {
    private UUID id;

    @Column(name = "current_step")
    private String currentStep;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    // useful to tell apart different kinds of Sagas supported by one system
    private String type;

    // TODO: Need to register successful messages + compensating messages for each step
    @Convert(converter = ListAttributeConverter.class)
    private List<SagaStep> steps = new ArrayList<>();

    public StepBuilder stepBuilder() {
        return new StepBuilder();
    }

    public class StepBuilder {
        public StepBuilder() {}

        public StepBuilder step(SagaStep sagaStep) {
            steps.add(sagaStep);
            return this;
        }

        public List<SagaStep> build() {
            return steps;
        }
    }

    public SagaInstance(String sagaType, List<SagaStep> sagaSteps) {
        if (sagaSteps.isEmpty()) {
            throw new RuntimeException("Saga instance must have at lease one step");
        }
        currentStep = sagaSteps.get(0).getKey();
        status = STARTED;
        type = sagaType;
        steps = sagaSteps;
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
}
