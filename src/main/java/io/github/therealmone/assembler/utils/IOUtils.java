package io.github.therealmone.assembler.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public final class IOUtils {

    public static String loadResource(String resource) {
        try (final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
             final StringWriter writer = new StringWriter())
        {
            int bt;
            while ((bt = input.read()) != -1) {
                writer.write(bt);
            }
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
