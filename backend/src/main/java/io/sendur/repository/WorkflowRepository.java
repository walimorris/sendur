package io.sendur.repository;

import io.sendur.domain.workflow.Workflow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {

    /**
     * Find {@link Workflow} by business workflowId.
     *
     * @param workflowId {@link String workflowId}
     *
     * @return {@link Optional<Workflow>}
     */
    Optional<Workflow> findByWorkflowId(String workflowId);
}
