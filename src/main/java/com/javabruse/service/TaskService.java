package com.javabruse.service;

import com.javabruse.DTO.TaskRequest;
import com.javabruse.DTO.TaskResponse;
import com.javabruse.converter.TaskConverter;
import com.javabruse.model.Task;
import com.javabruse.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService implements EntityService<TaskResponse, TaskRequest> {
    private final TaskRepo taskRepo;
    private final TaskConverter taskConverter;

    @Override
    public List<TaskResponse> update(TaskRequest task, UUID userUUID) {
        Optional<Task> taskOld = taskRepo.findByIdAndUserId(task.getId(), userUUID);
        if (taskOld.isPresent()) {
            taskRepo.save(taskConverter.taskRequestUpdate(task, taskOld.get()));
        }
        return getAll(userUUID);
    }

    @Override
    public List<TaskResponse> delete(UUID id, UUID userUUID) {
        Optional<Task> task = taskRepo.findByIdAndUserId(id, userUUID);
        if (task.isPresent()) {
            taskRepo.delete(task.get());
        }
        return getAll(userUUID);
    }

    @Override
    public List<TaskResponse> add(TaskRequest task, UUID userUUID) {
        taskRepo.save(taskConverter.taskRequestNew(task, userUUID));
        return getAll(userUUID);
    }

    @Override
    public List<TaskResponse> getAll(UUID userUUID) {
        return taskRepo.findByUserId(userUUID).stream().map(taskConverter::taskToTaskResponse).toList();
    }
}
