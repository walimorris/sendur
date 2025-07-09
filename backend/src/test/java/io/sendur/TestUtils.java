package io.sendur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sendur.factories.ObjectMapperFactory;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.io.InputStream;

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
