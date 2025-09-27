package com.javabruse.service;


import java.util.List;
import java.util.UUID;

public interface EntityService<T> {
    List<T> update(T t, UUID userUUID);

    List<T> delete(UUID id, UUID userUUID);

    List<T> add(T t, UUID userUUID);

    List<T> getAll(UUID userUUID);
}
