package com.javabruse.service;

import com.javabruse.DTO.PhotoRequest;
import com.javabruse.DTO.PhotoResponse;
import com.javabruse.converter.PhotoConverter;
import com.javabruse.model.Photo;
import com.javabruse.repository.PhotoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService implements EntityService<PhotoResponse, PhotoRequest> {

    private final PhotoRepo photoRepo;
    private final PhotoConverter photoConverter;
    private final S3PresignedUrlService serviceS3;

    @Override
    public List<PhotoResponse> update(PhotoRequest photoRequest, UUID userUUID) {
        Optional<Photo> photoOld = photoRepo.findById(photoRequest.getId());
        if (photoOld.isPresent()) {
            photoRepo.save(photoConverter.photoRequestToUpdatePhoto(photoRequest, photoOld.get()));
        }
        return getAllByTask(photoRequest.getTaskId(), userUUID);
    }

    @Override
    public List<PhotoResponse> delete(UUID id, UUID userUUID) {
        Optional<Photo> photo = photoRepo.findById(id);
        UUID taskUUID = null;
        if (photo.isPresent()) {
            taskUUID = photo.get().getTask().getId();
            photoRepo.delete(photo.get());
        }
        return getAllByTask(taskUUID, userUUID);
    }

    @Override
    public List<PhotoResponse> add(PhotoRequest photoRequest, UUID userUUID) {
        photoRepo.save(photoConverter.photoRequestToNewPhoto(photoRequest, userUUID));
        return getAllByTask(photoRequest.getTaskId(), userUUID);
    }

    @Override
    public List<PhotoResponse> getAll(UUID userUUID) {
        log.info("--получение из бд всех фото-----------------этап 1----------------");
        List<Photo> list = photoRepo.findByUserId(userUUID);
        log.info("размер полученного массива: "+list.size());
        log.info("-------конвертация------------этап 2----------------");
        List<PhotoResponse> list2 = list.stream().map(photoConverter::PhotoToPhotoResponse).toList();
        log.info("размер полученного массива: "+list2.size());
        log.info("-------добавления патч------------этап 3----------------");
        list2.stream().map(p->{
                p.setFilePath(getPathViewPhoto(p.getId(),userUUID));
                return p;
        }).toList();
        log.info("добавил временный патч на каждое фото: "+list2.size());
        log.info("перывй патч: " +list2.get(0).getFilePath());
        log.info("-------отправка------------этап 4----------------");
        return list2;
//        return photoRepo.findByUserId(userUUID).stream()
//                .map(photo -> {
//                    PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
//                    response.setFilePath(getPathViewPhoto(response.getId(), userUUID));
//                    return response;
//                }).toList();
    }

    public List<PhotoResponse> getAllByTask(UUID taskUUID, UUID userUUID) {
        return photoRepo.findByTaskIdAndUserId(taskUUID, userUUID).stream()
                .map(photo -> {
                    PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
                    response.setFilePath(getPathViewPhoto(response.getId(), userUUID));
                    return response;
                }).toList();
    }

    public PhotoResponse getPhoto(UUID photoUUID, UUID userUUID) {
        Photo photo = photoRepo.findById(photoUUID)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        if (!photo.getUserId().equals(userUUID)) {
            throw new RuntimeException("Access denied");
        }
        PhotoResponse photoResponse = photoConverter.PhotoToPhotoResponse(photo);
        photoResponse.setFilePath(getPathViewPhoto(photoResponse.getId(), userUUID));
        return photoResponse;
    }

    private String getPathViewPhoto(UUID photoUUID, UUID userUUID) {
        StringBuilder sb = new StringBuilder();
        sb.append(userUUID);
        sb.append("/photos/");
        sb.append(photoUUID);
        return serviceS3.generatePresignedViewUrl(sb.toString());
    }
}
