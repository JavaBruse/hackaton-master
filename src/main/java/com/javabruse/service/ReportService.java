package com.javabruse.service;

import com.javabruse.model.*;
import com.javabruse.repository.TaskRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ReportService {
    private final TaskRepo taskRepo;
    private final PhotoService photoService;
    public List<PhotoExportDTO> getReportForExport(UUID taskId, UUID userId) {
        Optional<Task> taskOptional = taskRepo.findByIdAndUserId(taskId, userId);
        if (taskOptional.isPresent()) {
            List<PhotoExportDTO> list = new ArrayList<>();
            for (Photo photo: taskOptional.get().getPhotos()){
                for (ConstructMetadata c: photo.getConstructMetadata()){
                    list.add(photoToPhotoExportDTO(photo, c));
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    private PhotoExportDTO photoToPhotoExportDTO(Photo photo, ConstructMetadata constructMetadata){
        PhotoExportDTO photoExportDTO = new PhotoExportDTO();
        photoExportDTO.setName(photo.getName());
        double sizeInMB = photo.getFileSize() / 1024.0 / 1024.0;
        photoExportDTO.setFileSize(String.format("%.3f mb", sizeInMB));
        photoExportDTO.setSrc(photoService.getPhoto(photo.getId(),photo.getUserId()).getFilePathComplete());
        photoExportDTO.setNumber(constructMetadata.getPosition());
        photoExportDTO.setAddress(constructMetadata.getAddress());
        photoExportDTO.setLatitude(constructMetadata.getLatitude());
        photoExportDTO.setLongitude(constructMetadata.getLongitude());
        return photoExportDTO;
    }
}
