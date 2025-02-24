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

    @SuppressWarnings("null")
    public String storeFile(MultipartFile file, String path) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Resolve the full target directory from the provided path
        Path targetDir = rootLocation.resolve(path).normalize();

        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path destinationFile = targetDir.resolve(fileName).normalize().toAbsolutePath();
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored file: {} at {}", fileName, destinationFile);
        return "/uploads/" + path + "/" + fileName;
    }

    public void deleteFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            try {
                // Remove leading "/uploads/" if present, since it's already part of
                // rootLocation
                String cleanedPath = filePath.startsWith("/uploads/") ? filePath.substring("/uploads/".length())
                        : filePath;
                Path file = rootLocation.resolve(cleanedPath).normalize();

                if (Files.exists(file)) {
                    Files.delete(file);
                    log.info("Deleted file: {}", filePath);

                    // Clean up empty parent directories
                    Path parent = file.getParent();
                    while (parent != null && !parent.equals(rootLocation) && Files.isDirectory(parent)
                            && isDirectoryEmpty(parent)) {
                        Files.delete(parent);
                        log.info("Deleted empty directory: {}", parent);
                        parent = parent.getParent();
                    }
                } else {
                    log.warn("File does not exist: {}", filePath);
                }
            } catch (IOException e) {
                log.error("Error deleting file or directory: {}", filePath, e);
            }
        } else {
            log.warn("File path is null or empty, skipping deletion");
        }
    }

    public void deleteFiles(List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            filePaths.forEach(this::deleteFile);
        } else {
            log.warn("File paths list is null or empty, no files to delete");
        }
    }

    // Helper method to check if a directory is empty
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
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
