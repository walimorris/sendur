package io.sendur.utils;

public class PromptUtils {

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
}
