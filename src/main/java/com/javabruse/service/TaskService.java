package com.javabruse.service;

import com.javabruse.model.Task;
import com.javabruse.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService implements EntityService<Task> {
    private final TaskRepo taskRepo;

    @Override
    public List<Task> update(Task task, UUID userUUID) {
        Optional<Task> taskOld = taskRepo.findByIdAndUserId(task.getId(), userUUID);
        if (taskOld.isPresent()) {
            Task task1 = taskOld.get();
            task1.setUpdatedAt(Instant.now().toEpochMilli());
            task1.setStatus(task.getStatus());
            task1.setName(task.getName());
            taskRepo.save(task1);
        }
        return getAll(userUUID);
    }

    @Override
    public List<Task> delete(UUID id, UUID userUUID) {
        Optional<Task> task = taskRepo.findByIdAndUserId(id, userUUID);
        if (task.isPresent()) {
            taskRepo.delete(task.get());
        }
        return getAll(userUUID);
    }

    @Override
    public List<Task> add(Task task, UUID userUUID) {
        task.setUserId(userUUID);
        taskRepo.save(task);
        return getAll(userUUID);
    }

    @Override
    public List<Task> getAll(UUID userUUID) {
        return taskRepo.findByUserId(userUUID);
    }
}
