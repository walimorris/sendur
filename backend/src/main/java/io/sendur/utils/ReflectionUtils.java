package io.sendur.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class ReflectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    private ReflectionUtils() {
        // util can not be instantiated
    }

    /**
     * Inspects an object for null values. If all values are null, it returns true.
     * Otherwise, false. This method is useful for inspecting custom POJOs.
     *
     * @param obj {@link Object} to check
     *
     * @return boolean
     */
    public static boolean isAllFieldsNull(Object obj) {
        if (obj == null) {
            return true;
        }
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(obj) != null) {
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to inspect object fields: {}", e.getMessage());
            return false;
        }
    }
}
