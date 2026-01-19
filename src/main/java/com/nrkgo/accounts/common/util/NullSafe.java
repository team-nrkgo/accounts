package com.nrkgo.accounts.common.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class NullSafe {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    /**
     * Checks if a string is not null and not empty.
     */
    public static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Checks if a collection is not null and not empty.
     */
    public static boolean isValidCollection(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    /**
     * Checks if a map is not null and not empty.
     */
    public static boolean isValidMap(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    /**
     * Checks if an integer is not null and greater than zero.
     */
    public static boolean isValidInteger(Integer value) {
        return value != null && value > 0;
    }

    /**
     * Checks if a long is not null and greater than zero.
     */
    public static boolean isValidLong(Long value) {
        return value != null && value > 0;
    }

    /**
     * Checks if a string represents a valid numeric value.
     */
    public static boolean isNumeric(String strNumber) {
        if (!isValidString(strNumber)) {
            return false;
        }
        return NUMERIC_PATTERN.matcher(strNumber).matches();
    }
}
