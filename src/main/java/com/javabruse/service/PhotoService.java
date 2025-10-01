package com.javabruse.service;

import com.javabruse.DTO.PhotoRequest;
import com.javabruse.DTO.PhotoResponse;
import com.javabruse.converter.PhotoConverter;
import com.javabruse.model.Photo;
import com.javabruse.model.Status;
import com.javabruse.repository.PhotoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            if (!photo.get().getStatus().equals(Status.IN_PROGRESS)) {
                taskUUID = photo.get().getTask().getId();
                StringBuilder sb = new StringBuilder();
                sb.append(userUUID);
                sb.append("/photos/");
                sb.append(photo.get().getFilePathOriginal());
                serviceS3.deleteObject(sb.toString());
                if (photo.get().getFilePathComplete() != null) {
                    StringBuilder sb2 = new StringBuilder();
                    sb.append(userUUID);
                    sb.append("/photos/");
                    sb.append(photo.get().getFilePathComplete());
                    serviceS3.deleteObject(sb.toString());
                }
                photoRepo.delete(photo.get());
            }
        }
        return getAllByTask(taskUUID, userUUID);
    }

    @Override
    public List<PhotoResponse> add(PhotoRequest photoRequest, UUID userUUID) {
        photoRepo.save(photoConverter.photoRequestToNewPhoto(photoRequest, userUUID));
        return getAllByTask(photoRequest.getTaskId(), userUUID);
    }

    public Photo addAndReturnPhoto(PhotoRequest photoRequest, UUID userUUID) {
        return  photoRepo.save(photoConverter.photoRequestToNewPhoto(photoRequest, userUUID));
    }

    @Override
    public List<PhotoResponse> getAll(UUID userUUID) {
        return photoRepo.findByUserId(userUUID).stream()
                .map(photo -> {
                    PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
                    response.setFilePathOriginal(getPathViewPhoto(response.getFilePathOriginal(), userUUID));
                    response.setFilePathComplete(getPathViewPhoto(response.getFilePathComplete(), userUUID));
                    return response;
                }).toList();
    }

    public List<PhotoResponse> getAllByTask(UUID taskUUID, UUID userUUID) {
        return photoRepo.findByTaskIdAndUserId(taskUUID, userUUID).stream()
                .map(photo -> {
                    PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
                    response.setFilePathOriginal(getPathViewPhoto(response.getFilePathOriginal(), userUUID));
                    response.setFilePathComplete(getPathViewPhoto(response.getFilePathComplete(), userUUID));
                    return response;
                }).toList();
    }

    public PhotoResponse getPhoto(UUID photoUUID, UUID userUUID) {
        Photo photo = photoRepo.findById(photoUUID)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        if (!photo.getUserId().equals(userUUID)) {
            throw new RuntimeException("Access denied");
        }
        PhotoResponse response = photoConverter.PhotoToPhotoResponse(photo);
        response.setFilePathOriginal(getPathViewPhoto(response.getFilePathOriginal(), userUUID));
        if (response.getFilePathComplete()==null){
            response.setFilePathComplete(getPathViewPhoto(response.getFilePathComplete(), userUUID));
        }
        return response;
    }

    public String getPathViewPhoto(String filePath, UUID userUUID) {
        StringBuilder sb = new StringBuilder();
        sb.append(userUUID);
        sb.append("/photos/");
        sb.append(filePath);
        return serviceS3.generatePresignedViewUrl(sb.toString());
    }
}
