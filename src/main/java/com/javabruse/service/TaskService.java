package com.javabruse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javabruse.DTO.PhotoMessage;
import com.javabruse.DTO.TaskMessage;
import com.javabruse.DTO.TaskRequest;
import com.javabruse.DTO.TaskResponse;
import com.javabruse.converter.TaskConverter;
import com.javabruse.converter.TaskMessageConverter;
import com.javabruse.model.ConstructMetadata;
import com.javabruse.model.Photo;
import com.javabruse.model.Status;
import com.javabruse.model.Task;
import com.javabruse.repository.PhotoRepo;
import com.javabruse.repository.TaskRepo;
import com.javabruse.service.kafka.KafkaConsumerService;
import com.javabruse.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService implements EntityService<TaskResponse, TaskRequest> {
    private final TaskRepo taskRepo;
    private final TaskConverter taskConverter;
    private final KafkaProducerService kafkaProducerService;
    private final TaskMessageConverter taskMessageConverter;
    private final PhotoRepo photoRepo;


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

    public List<TaskResponse> sendTaskToKafka(UUID taskID, UUID userUUID) {
        Optional<Task> taskOpt = taskRepo.findByIdAndUserId(taskID, userUUID);
        if (taskOpt.isPresent()) {
            taskOpt.get().setStatus(Status.IN_PROGRESS);
            for (Photo photo : taskOpt.get().getPhotos()) {
                photo.setStatus(Status.IN_PROGRESS);
            }
            taskRepo.save(taskOpt.get());
            List<TaskMessage> taskMessagesList = taskMessageConverter.taskToTaskMessageList(taskOpt.get());
            for (TaskMessage message : taskMessagesList) {
                kafkaProducerService.sendTransferRequestTask(message);
            }
        }
        return getAll(userUUID);
    }

    public void listenTaskFromKafka(TaskMessage taskMessage) {
        log.info("------------------Этап-1 listenTaskFromKafka");
        Photo photo = taskMessageConverter.taskMessageToPhoto(taskMessage, Status.COMPLETED);
//        for (ConstructMetadata constructMetadata : photo.getConstructMetadata()) {
//            log.info("------------------Этап-2 listenTaskFromKafka");
//
//            constructMetadata.setAddress(getAddress(constructMetadata));
//        }
        log.info("------------------Этап-3 listenTaskFromKafka");
        photoRepo.save(photo);
        boolean allCompleted = true;
        log.info("------------------Этап-4 listenTaskFromKafka");
        Optional<Task> task = taskRepo.findById(photo.getTask().getId());
        log.info("------------------Этап-5 listenTaskFromKafka");

        if (task.isPresent()) {
            for (Photo data : task.get().getPhotos()) {
                if (!data.getStatus().equals(Status.COMPLETED)) {
                    allCompleted = false;
                    break;
                }
            }
        }
        log.info("------------------Этап-6 listenTaskFromKafka");

        if (allCompleted && task.isPresent()) {
            task.get().setStatus(Status.COMPLETED);
            taskRepo.save(task.get());
        }
    }


    private String getAddress(ConstructMetadata constructMetadata) {
        if (constructMetadata.getLatitude() == null || constructMetadata.getLongitude() == null) {
            return "";
        }

        String apiUrl = String.format(
                "https://geocode-maps.yandex.ru/v1/?apikey=ef7cffb6-6ad2-406b-ad30-1e6048ada555&geocode=%s,%s&format=json",
                constructMetadata.getLongitude(),
                constructMetadata.getLatitude()
        );
        try {
            var url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Парсим JSON и извлекаем адрес
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.toString());
            JsonNode features = root.path("response").path("GeoObjectCollection").path("featureMember");

            if (features.size() > 0) {
                return features.get(0).path("GeoObject").path("name").asText();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }
//    https://geocode-maps.yandex.ru/v1/?apikey=ef7cffb6-6ad2-406b-ad30-1e6048ada555&geocode=25.194867,55.274795&format=json
}
