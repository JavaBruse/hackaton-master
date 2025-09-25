package com.javabruse.repository;

import com.javabruse.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepo  extends JpaRepository<Task, UUID> {
}
