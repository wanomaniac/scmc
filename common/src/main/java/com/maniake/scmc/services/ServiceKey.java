package com.maniake.scmc.services;

public final class ServiceKey<T> {
    private final Class<T> type;

    private ServiceKey(Class<T> type) {
        this.type = type;
    }

    public static <T> ServiceKey<T> of(Class<T> type) {
        return new ServiceKey<>(type);
    }

    public Class<T> type() {
        return type;
    }
}
