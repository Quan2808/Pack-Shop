package com.packshop.client.common.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        // Normalize file name
        @SuppressWarnings("null")
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Add timestamp to prevent duplicate names
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));
        fileName = UUID.randomUUID().toString() + fileExtension;

        // Copy file to upload directory
        Path targetLocation = Paths.get(uploadDir).resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    public void deleteFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                Path filePath = Paths.get(uploadDir).resolve(fileName);
                Files.deleteIfExists(filePath);
                log.info("Deleted file: {}", fileName);
            } catch (IOException e) {
                log.error("Error deleting file: {}", fileName, e);
            }
        }
    }

    public void deleteFiles(List<String> fileNames) {
        if (fileNames != null) {
            fileNames.forEach(this::deleteFile);
        }
    }
}
