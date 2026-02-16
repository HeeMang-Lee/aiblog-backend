package com.aiblog.global.storage;

import com.aiblog.global.exception.BusinessException;
import com.aiblog.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorageService implements FileStorageService {

  private final Path uploadDir;

  public LocalFileStorageService(
      @Value("${app.storage.local.upload-dir}") String uploadDir) {
    this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    createDirectoryIfNotExists(this.uploadDir);
  }

  @Override
  public String store(MultipartFile file, String directory) {
    String originalFilename = StringUtils.cleanPath(
        file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
    String extension = extractExtension(originalFilename);
    String storedFilename = UUID.randomUUID() + extension;

    Path targetDir = uploadDir.resolve(directory).normalize();
    createDirectoryIfNotExists(targetDir);

    Path targetPath = targetDir.resolve(storedFilename);
    try {
      file.transferTo(targetPath);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    return "/" + uploadDir.getFileName() + "/" + directory + "/"
        + storedFilename;
  }

  @Override
  public void delete(String filePath) {
    Path path = uploadDir.getParent()
        .resolve(filePath.startsWith("/") ? filePath.substring(1) : filePath)
        .normalize();
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
    }
  }

  private String extractExtension(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex >= 0) ? filename.substring(dotIndex) : "";
  }

  private void createDirectoryIfNotExists(Path path) {
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }
}
