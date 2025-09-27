package com.javabruse.service;


import java.util.List;
import java.util.UUID;

public interface EntityService<T,Y> {
    List<T> update(Y y, UUID userUUID);

    List<T> delete(UUID id, UUID userUUID);

    List<T> add(Y y, UUID userUUID);

    List<T> getAll(UUID userUUID);
}
