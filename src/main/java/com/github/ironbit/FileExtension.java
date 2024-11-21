package com.github.ironbit;

/**
 * Enum that contains the file extensions that can be handled.
 */
public enum FileExtension {
    JSON, XML, CSV, TXT;

    /**
     * Checks if the given value is a valid file extension.
     *
     * @param value the value to check
     * @return true if the value is a valid file extension, false otherwise
     */
    static boolean contains(String value) {
        for (FileExtension extension : FileExtension.values()) {
            if (extension.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
