package com.github.ironbit;

public enum FileExtension {
    JSON, XML, CSV, TXT;

    public static boolean contains(String value) {
        for (FileExtension extension : FileExtension.values()) {
            if (extension.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
