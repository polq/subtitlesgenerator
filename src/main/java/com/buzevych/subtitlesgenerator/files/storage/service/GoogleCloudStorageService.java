package com.buzevych.subtitlesgenerator.files.storage.service;

import com.buzevych.subtitlesgenerator.files.exceptions.InvalidStorageException;
import com.google.cloud.storage.*;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Class that is used to work with Google Cloud Storage and store and retrieve files from there. */
@Service
@Slf4j
@NoArgsConstructor
public class GoogleCloudStorageService {

  @Value("${google.cloud.storage.projectId}")
  private String projectId;

  @Value("${google.cloud.storage.bucketName}")
  private String bucketName;

  @Value("${google.cloud.storage.filePrefix}")
  private String fileNamePrefix;

  private Storage storage;

  GoogleCloudStorageService(Storage storage) {
    this.storage = storage;
  }

  @PostConstruct
  void init() {
    storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
  }

  /**
   * Stores file in the GCS, with the preset bucketName and initialized {@link Storage}
   *
   * @param sourceFile file to be stored
   * @return URI of file that is stored, in the following way : gs://speech-to-text-project-bucket/flac.flac
   * @throws InvalidStorageException in case there was an error with the file
   */
  public String store(File sourceFile) {
    String fileName = sourceFile.getName();
    BlobId blobId = BlobId.of(bucketName, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    try {
      storage.create(blobInfo, Files.readAllBytes(sourceFile.toPath()));
    } catch (IOException e) {
      log.error("There was an error while saving {} file: {}", fileName, e);
      throw new InvalidStorageException(
          "There was an error while trying to save " + fileName + " to Google Cloud Storage", e);
    }
    log.info("File {} has been successfully store in the {} bucket", fileName, bucketName);
    return fileNamePrefix + bucketName + "/" + fileName;
  }

  /**
   *  Used to get a {@link File} object from preset GCS with the specified name
   * @param fileName name of a file that is to be loaded (e.g "flac.flac")
   * @return {@link File} loaded file
   * @throws IOException
   */
  public File get(String fileName) throws IOException {
    Blob blob = storage.get(BlobId.of(bucketName, fileName));
    File file = File.createTempFile("temp", fileName);
    blob.downloadTo(file.toPath());
    return file;
  }
}
