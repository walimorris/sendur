package io.sendur.repository;

import io.sendur.domain.execution.Execution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExecutionsRepository extends MongoRepository<Execution, String> {
}
