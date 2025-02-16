package com.packshop.client.common.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            // Get the absolute path to the resources directory
            String projectDir = System.getProperty("user.dir");
            rootLocation = Paths.get(projectDir, "src", "main", "resources", "static", "uploads");

            // Create directory if it doesn't exist
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            log.info("File storage location initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file");
            }

            // Generate unique filename
            @SuppressWarnings("null")
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + fileExtension;

            // Resolve the full path
            Path destinationFile = rootLocation.resolve(fileName).normalize().toAbsolutePath();

            // Validate the destination path
            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new IllegalArgumentException("Cannot store file outside current directory");
            }

            // Save the file
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored file: {} at {}", fileName, destinationFile);
            return fileName;

        } catch (IOException e) {
            throw new IOException("Failed to store file", e);
        }
    }

    public void deleteFile(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            try {
                Path file = rootLocation.resolve(fileName);
                Files.deleteIfExists(file);
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

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = rootLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }
}