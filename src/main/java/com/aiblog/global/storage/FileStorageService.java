package com.aiblog.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  String store(MultipartFile file, String directory);

  void delete(String filePath);
}
