package com.sonar.trading.utils;

import java.io.InputStream;
import java.net.URL;

public class Resources {
    public static InputStream getResourceStream(String responsePath) {
        return Resources.class.getClassLoader().getResourceAsStream(responsePath);
    }

    public static String getResourcePath(String responsePath) {
        return getResourceURL(responsePath).toExternalForm();
    }

    public static URL getResourceURL(String responsePath) {
        return Resources.class.getClassLoader().getResource(responsePath);
    }
}
