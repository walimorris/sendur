package io.sendur.services;

import io.sendur.Violation;
import io.sendur.utils.PromptUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiGuardRailsService {

    @Value("${ai.prompts.max-length}")
    private int maxLength;

    public List<Violation> validatePrompt(String prompt) {
        List<Violation> violations = new ArrayList<>();
        if (PromptUtils.isPromptTooLong(prompt, maxLength)) {
            violations.add(Violation.PROMPT_TOO_LONG);
        }
        return violations;
    }
}
