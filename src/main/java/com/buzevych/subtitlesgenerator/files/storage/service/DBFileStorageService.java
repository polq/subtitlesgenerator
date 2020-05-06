package com.buzevych.subtitlesgenerator.files.storage.service;

import com.buzevych.subtitlesgenerator.files.exceptions.InvalidStorageException;
import com.buzevych.subtitlesgenerator.files.exceptions.StorageFileNotFoundException;
import com.buzevych.subtitlesgenerator.files.model.DBFile;
import com.buzevych.subtitlesgenerator.files.storage.repository.DBFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Service class that is used to store files in the database */
@Service
public class DBFileStorageService {

  private DBFileRepository dbFileRepository;

  @Autowired
  public DBFileStorageService(DBFileRepository dbFileRepository) {
    this.dbFileRepository = dbFileRepository;
  }

  /**
   * Method that is used to store {@link MultipartFile} file in a convenient way
   *
   * @param multipartFile source file that is to be stored
   * @return {@link DBFile} instance of stored file
   */
  public DBFile storeFile(MultipartFile multipartFile) {
    try {
      DBFile dbFile = DBFile.of(multipartFile);
      return dbFileRepository.save(dbFile);
    } catch (IOException cause) {
      throw new InvalidStorageException(
          "Could not store file " + multipartFile.getOriginalFilename(), cause);
    }
  }

  /**
   * Method that is used to get file by it's ID
   *
   * @param fileId corresponds to file ID
   * @return {@link DBFile} instance of stored file
   * @throws StorageFileNotFoundException in case there is no such file
   */
  public DBFile getFile(Long fileId) {
    return dbFileRepository
        .findById(fileId)
        .orElseThrow(() -> new StorageFileNotFoundException("File not found with id " + fileId));
  }
}
