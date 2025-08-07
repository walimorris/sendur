package io.sendur.domain.execution;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class Executions {

    @JsonAlias("data")
    private List<Execution> executions;
    private String nextCursor;
}
