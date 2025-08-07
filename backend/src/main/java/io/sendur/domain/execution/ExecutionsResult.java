package io.sendur.domain.execution;

import java.util.List;
import java.util.Optional;

public record ExecutionsResult(int statusCode, List<Execution> executions, Optional<String> message) {}
