package com.vikson.projects.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ResourceUtils {

    private ResourceUtils() {
    }

    public static String readResourceAsString(String resourcePath) {
        return readResourceAsString(resourcePath, Charset.defaultCharset());
    }

    public static String readResourceAsString(String resourcePath, Charset charset) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, charset);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
