package com.javabruse.repository;

import com.javabruse.model.PhotoMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhotoMetadataRepo extends JpaRepository<PhotoMetadata, UUID> {
}
