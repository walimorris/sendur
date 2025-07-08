package io.sendur.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.moderation.*;
import org.springframework.ai.openai.OpenAiModerationModel;

import java.lang.reflect.Field;
import java.util.*;

public class PromptUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromptUtils.class);

    private PromptUtils() {
        // util can not be constructed
    }

    /**
     * Checks if prompt is over the max length.
     *
     * @param prompt llm prompt
     * @param max max length of prompt
     *
     * @return boolean
     */
    public static boolean isPromptTooLong(String prompt, int max) {
        return prompt.length() > max;
    }

    /**
     * Checks if prompt is flagged by
     * <a href="https://docs.spring.io/spring-ai/reference/api/moderation/openai-moderation.html">
     *     OpenAI Moderation API
     * </a>
     *
     * @param prompt {@link String} prompt
     * @param moderationModel {@link OpenAiModerationModel} moderation model
     *
     * @return boolean
     */
    public static boolean hasPromptModerationFlags(String prompt, OpenAiModerationModel moderationModel) {
        Moderation moderation = getModerationPromptResult(prompt, moderationModel);
        List<ModerationResult> result = moderation.getResults();
        for (ModerationResult moderationResult : result) {
            if (moderationResult.isFlagged()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get Moderation Flags from given prompt.
     *
     * @param prompt {@link String} prompt
     * @param moderationModel {@link OpenAiModerationModel} moderation model
     *
     * @return {@link String} flags and their values, returns all flags and values regardless of number of hits
     */
    public static String getModerationFlags(String prompt, OpenAiModerationModel moderationModel) {
        Moderation moderation = getModerationPromptResult(prompt, moderationModel);
        return moderationValidationResult(moderation);
    }

    /**
     * Get {@linkplain Moderation moderation prompt result output} from given prompt. This moderation
     * object can be utilized in analyzing a prompts moderation output.
     *
     * @param prompt {@link String} prompt
     * @param moderationModel {@link OpenAiModerationModel} moderation model
     *
     * @return {@link Moderation}
     */
    private static Moderation getModerationPromptResult(String prompt, OpenAiModerationModel moderationModel) {
        ModerationPrompt moderationPrompt = new ModerationPrompt(prompt);
        ModerationResponse moderationResponse = moderationModel.call(moderationPrompt);
        return moderationResponse.getResult().getOutput();
    }

    /**
     * Builds a moderation result string. This result string consists of all the moderation {@link Categories}
     * field properties and their values. These flags indicate what moderation flags are hit when this
     * moderation's prompt processed through the
     * <a href="https://docs.spring.io/spring-ai/reference/api/moderation/openai-moderation.html">
     *     OpenAI Moderation API
     * </a>
     *
     * @param moderation {@link Moderation}
     *
     * @return {@link String} string consisting of moderation flags and their values
     */
    private static String moderationValidationResult(Moderation moderation) {
        StringBuilder moderationViolationBuilder = new StringBuilder();
        moderationViolationBuilder.append("Flags: ");
        for (ModerationResult moderationResult : moderation.getResults()) {
            if (moderationResult.isFlagged()) {
                // get the flags
                Map<String, Boolean> moderationFlags = getModerationCategories(moderationResult);
                for (Map.Entry<String, Boolean> entry : moderationFlags.entrySet()) {
                    moderationViolationBuilder.append(entry.getKey())
                            .append(": ")
                            .append(entry.getValue())
                            .append(" ");
                }
                moderationViolationBuilder.append("\n");
            }
        }
        return moderationViolationBuilder.toString();
    }

    /**
     * Get a properties {@link Map} containing the {@link Categories} class category properties and
     * their values based on the given {@link ModerationResult} object. This is done by getting a
     * list of all possible category names and using Java reflection to pull the value of each
     * category property, given the passed {@link ModerationResult}.
     *
     * @param moderationResult
     * @return
     */
    private static Map<String, Boolean> getModerationCategories(ModerationResult moderationResult) {
        List<Field> categoriesFields = getSpringAiCategoriesFields(); // get all category class fields
        Categories categoriesObject = moderationResult.getCategories(); // get the categories result

        Map<String, Boolean> categories = new HashMap<>();
        // for each category field property, set accessible and pull the value from the categories object
        for (Field categoryField : categoriesFields) {
            categoryField.setAccessible(true);
            try {
                Object categoryValue = categoryField.get(categoriesObject);
                if (categoryValue instanceof Boolean) {
                    categories.put(categoryField.getName(), (Boolean) categoryValue);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("Error getting categories values: {}", e.getMessage());
            }
        }
        return categories;
    }

    /**
     * Get all field property names from the {@link Categories} class.
     *
     * @return {@link List<Field>}
     */
    private static List<Field> getSpringAiCategoriesFields() {
        return new ArrayList<>(Arrays.asList(Categories.class.getDeclaredFields()));
    }
}
