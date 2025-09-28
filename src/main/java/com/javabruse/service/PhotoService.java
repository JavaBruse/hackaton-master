package com.javabruse.service;

import com.javabruse.DTO.PhotoRequest;
import com.javabruse.DTO.PhotoResponse;
import com.javabruse.converter.PhotoConverter;
import com.javabruse.model.Photo;
import com.javabruse.repository.PhotoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        return photoRepo.findByUserId(userUUID).stream()
                .map(photo -> {
                    PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
                    response.setFilePath(getPathViewPhoto(response.getId(), userUUID));
                    return response;
                }).toList();
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
        PhotoResponse photoResponse = photoConverter.PhotoToPhotoResponse(photoRepo.findById(photoUUID).orElseThrow());
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
