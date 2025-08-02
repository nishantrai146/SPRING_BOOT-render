package com.lit.ims.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public String storeFile(MultipartFile file, String subfolder) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path subfolderPath = rootLocation.resolve(subfolder);
            Files.createDirectories(subfolderPath);
            Path destination = subfolderPath.resolve(fileName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return subfolder + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    public Resource loadFile(String path) {
        try {
            Path file = rootLocation.resolve(path).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + path);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + path, ex);
        }
    }
}
