package com.maniake.scmc.services;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ServiceLoader;

public final class ServicesManager {

    private static final Map<ServiceKey<?>, Object> CACHE = new ConcurrentHashMap<>();

    private ServicesManager() {}

    public static <T> T get(ServiceKey<T> key) {

        Object existing = CACHE.get(key);
        if (existing != null) {
            return key.type().cast(existing);   // safe cast
        }

        // Load from ServiceLoader
        T loaded = ServiceLoader.load(key.type())
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No service for: " + key.type().getName()));

        CACHE.put(key, loaded);
        return loaded;
    }
}
