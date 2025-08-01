package io.sendur.service.impl;

import io.sendur.Violation;
import io.sendur.utils.PromptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiModerationModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class AiGuardRailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiGuardRailsService.class);

    @Value("${ai.prompts.max-length}")
    private int maxLength;

    private final OpenAiModerationModel moderationModel;

    @Autowired
    public AiGuardRailsService(OpenAiModerationModel moderationModel) {
        this.moderationModel = moderationModel;
    }

    public Map<String, String> validatePrompt(String prompt) {
        List<String> violations = new ArrayList<>();
        String moderationFlags = null;

        if (PromptUtils.isPromptTooLong(prompt, maxLength)) {
            violations.add(Violation.PROMPT_TOO_LONG.toString());
        }
        if (PromptUtils.hasPromptModerationFlags(prompt, moderationModel)) {
            moderationFlags = PromptUtils.getModerationFlags(prompt, moderationModel);
        }
        return buildGuardRailsError(violations, moderationFlags);
    }

    private Map<String, String> buildGuardRailsError(List<String> violations, String moderationFlags) {
        Map<String, String> errors = new HashMap<>();
        if (!ObjectUtils.isEmpty(violations)) {
            errors.put("violations", StringUtils.join(", ", violations));
        }
        if (StringUtils.isNotEmpty(moderationFlags)) {
            errors.put("moderation-flags", moderationFlags);
        }
        return errors;
    }
}
