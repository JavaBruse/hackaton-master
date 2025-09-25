package com.javabruse.repository;

import com.javabruse.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhotoRepo extends JpaRepository<Photo, UUID> {
}
