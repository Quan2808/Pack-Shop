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
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService() {
        String projectDir = System.getProperty("user.dir");
        this.rootLocation = Paths.get(projectDir, "src", "main", "resources", "static", "uploads");
        init();
    }

    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            log.info("File storage location initialized at: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file, String categoryName, String productName, boolean isThumbnail)
            throws IOException {
        if (file.isEmpty()) {

            throw new IllegalArgumentException("Failed to store empty file");
        }

        @SuppressWarnings("null")
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Define the directory structure
        Path categoryDir = rootLocation.resolve(categoryName);
        Path productDir = categoryDir.resolve(productName);
        Path targetDir = isThumbnail ? productDir : productDir.resolve("media");

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path destinationFile = targetDir.resolve(fileName).normalize().toAbsolutePath();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored file: {} at {}", fileName, destinationFile);
        return categoryName + "/" + productName + (isThumbnail ? "/" : "/media/") + fileName;
    }

    public void deleteFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                Path file = rootLocation.resolve(filePath).normalize();
                if (Files.exists(file)) {
                    Files.delete(file);
                    log.info("Deleted file: {}", filePath);

                    Path parent = file.getParent();
                    while (parent != null && !parent.equals(rootLocation)) {
                        try {
                            Files.delete(parent);
                            log.info("Deleted empty directory: {}", parent);
                            parent = parent.getParent();
                        } catch (IOException e) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error deleting file or directory: {}", filePath, e);
            }
        }
    }

    public void deleteFiles(List<String> filePaths) {
        if (filePaths != null) {
            filePaths.forEach(this::deleteFile);
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = rootLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + filePath, e);
        }
    }
}
