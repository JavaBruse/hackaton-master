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
        photoExportDTO.setFileSize(photo.getFileSize());
        photoExportDTO.setSrc("http://5.129.246.42/v1/photo/view/"+photo.getId()+"/2");
        photoExportDTO.setNumber(constructMetadata.getPosition());
        photoExportDTO.setAddress(constructMetadata.getAddress());
        photoExportDTO.setLatitude(constructMetadata.getLatitude());
        photoExportDTO.setLongitude(constructMetadata.getLongitude());
        return photoExportDTO;
    }
}
