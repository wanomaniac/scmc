package com.maniake.scmc.utils;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.*;

public class CustomValues {
    // Get a boolean from top-level custom properties
    public static Optional<Boolean> getBoolean(String key, IModInfo info) {
        Object value = info.getModProperties().get(key);
        if (value instanceof Boolean b) return Optional.of(b);
        return Optional.empty();
    }

    // Get a string from top-level custom properties
    public static Optional<String> getString(String key, IModInfo info) {
        Object value = info.getModProperties().get(key);
        if (value instanceof String s) return Optional.of(s);
        return Optional.empty();
    }

    // Get a set of strings from a list under the key
    public static Optional<Set<String>> getStringSet(String key, IModInfo info) {
        Object value = info.getModProperties().get(key);
        if (value instanceof List<?> list) {
            Set<String> set = new HashSet<>();
            for (Object o : list) {
                set.add(o.toString());
            }
            return Optional.of(set);
        }
        return Optional.empty();
    }

    // Get a map of strings
    public static Optional<Map<String, String>> getStringMap(String key, IModInfo info) {
        Object value = info.getModProperties().get(key);
        if (value instanceof Map<?, ?> map) {
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> {
                if (k != null && v != null) result.put(k.toString(), v.toString());
            });
            return Optional.of(result);
        }
        return Optional.empty();
    }

    // Nested helper if you have a sub-map
    public static Optional<Boolean> getBoolean(String key, Map<String, Object> object) {
        Object value = object.get(key);
        if (value instanceof Boolean b) return Optional.of(b);
        return Optional.empty();
    }

    public static Optional<String> getString(String key, Map<String, Object> object) {
        Object value = object.get(key);
        if (value instanceof String s) return Optional.of(s);
        return Optional.empty();
    }

    public static Optional<String[]> getStringArray(String key, Map<String, Object> object) {
        Object value = object.get(key);
        if (value instanceof List<?> list) {
            String[] arr = list.stream().map(Object::toString).toArray(String[]::new);
            return Optional.of(arr);
        }
        return Optional.empty();
    }

    public static Optional<Set<String>> getStringSet(String key, Map<String, Object> object) {
        Object value = object.get(key);
        if (value instanceof List<?> list) {
            Set<String> set = new HashSet<>();
            for (Object o : list) set.add(o.toString());
            return Optional.of(set);
        }
        return Optional.empty();
    }

    public static Optional<Map<String, String>> getStringMap(String key, Map<String, Object> object) {
        Object value = object.get(key);
        if (value instanceof Map<?, ?> map) {
            Map<String, String> result = new HashMap<>();
            map.forEach((k, v) -> {
                if (k != null && v != null) result.put(k.toString(), v.toString());
            });
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
