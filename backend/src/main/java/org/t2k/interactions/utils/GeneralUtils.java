package org.t2k.interactions.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 23/07/2015
 * Time: 10:58
 */
public class GeneralUtils {

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static File getResource(Class clazz, String resource) {
        return new File(clazz.getClassLoader().getResource(resource).getPath());
    }

    public static InputStream getResourceAsStream(Class clazz, String resource) {
        return clazz.getClassLoader().getResourceAsStream(resource);
    }

    public static String readResourcesAsString(Class clazz, String resourceLocalPath) throws IOException {
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = clazz.getClassLoader().getResourceAsStream(resourceLocalPath);
            java.util.Scanner s = new java.util.Scanner(resourceAsStream).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } finally {
            resourceAsStream.close();
        }
    }
}