package io.sendur.repository;

import io.sendur.domain.workflow.Workflow;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {
}
