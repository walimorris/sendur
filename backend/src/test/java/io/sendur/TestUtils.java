package io.sendur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.factories.ObjectMapperFactory;
import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * {@code TestUtils} provides a set of convenient utility methods to process various test cases.
 */
public class TestUtils {
    private static final ObjectMapper objectMapper = ObjectMapperFactory.create();

    private TestUtils() {
        // illegal instantiation
    }

    /**
     * Returns a given object from its resource path. This functionality takes a json resource and
     * serializes it into the given reference type. This test utility provides a convenient way to
     * test various objects throughout the codebase.
     *
     * @param resourcePath {@link String} path to json resource
     * @param typeReference {@link TypeReference<T>} type to morph resource into
     *
     * @return T
     *
     * @param <T>
     */
    public static <T> T getObjectFromJsonResourceInputStream(String resourcePath, TypeReference<T> typeReference) {
        InputStream inputStream = getResourceInputStream(resourcePath);
        if (ObjectUtils.anyNull(inputStream, typeReference)) {
            if (ObjectUtils.allNull(inputStream, typeReference)) {
                throw new IllegalArgumentException("InputStream and TypeReference are both null.");
            } else if (ObjectUtils.anyNull(inputStream)) {
                throw new IllegalArgumentException("InputStream is null for resource: " + resourcePath);
            } else {
                throw new IllegalArgumentException("TypeReference is null for type: " + typeReference.getType());
            }
        }
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stream from resource path: " + resourcePath, e);
        }
    }

    /**
     * Gets the content from the given resource as a string. This utility is helpful for common {@code Sendur}
     * utilities that read in resources, but can not be used in the test context. Instead, the common goal of
     * receiving string content from a resource can be fulfilled with this method.
     *
     * @param resourcePath resource path (coming from test resource directory)
     *
     * @return {@link String} content
     */
    public static String getJsonStringContentFromResource(String resourcePath) {
        try (InputStream inputStream = getResourceInputStream(resourcePath)) {
            if (ObjectUtils.isEmpty(inputStream)) {
                throw new IllegalArgumentException("InputStream is null, check resource is not empty: " + resourcePath);
            }
            String contentJson = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JsonNode contentTree = objectMapper.readTree(contentJson);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(contentTree);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read json string: " + e.getMessage());
        }
    }

    /**
     * Gets the raw content from the given resource as a string. This utility is helpful for common {@code Sendur}
     * utilities that read in resources, but can not be used in the test context. Instead, the common goal of
     * receiving string content from a resource can be fulfilled with this method. This method does not discriminate
     * content, invalid and ill-formatted content will be parsed as is.
     *
     * @param resourcePath resource path (coming from test resource directory)
     *
     * @return {@link String} content
     */
    public static String getRawStringContentFromResource(String resourcePath) {
        InputStream inputStream = getResourceInputStream(resourcePath);
        if (ObjectUtils.isEmpty(inputStream)) {
            throw new IllegalArgumentException("InputStream is null, check resource is not empty: " + resourcePath);
        }
        StringBuilder jsonBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read string: " + e.getMessage());
        }
        return jsonBuilder.toString();
    }

    /**
     * Get resource {@link InputStream} from given resource path.
     *
     * @param resourcePath {@link String} class resource path
     *
     * @return {@link InputStream}
     */
    private static InputStream getResourceInputStream(String resourcePath) {
        return TestUtils.class.getClassLoader().getResourceAsStream(resourcePath);
    }
}
