package com.buzevych.subtitlesgenerator.files.storage.service;

import com.buzevych.subtitlesgenerator.files.exceptions.InvalidStorageException;
import com.buzevych.subtitlesgenerator.files.exceptions.StorageFileNotFoundException;
import com.buzevych.subtitlesgenerator.files.model.DBFile;
import com.buzevych.subtitlesgenerator.files.storage.repository.DBFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DBFileStorageServiceTest {

  @Mock DBFileRepository dbFileRepository;
  DBFileStorageService dbFileStorageService;

  @BeforeEach
  void init() {
    dbFileStorageService = new DBFileStorageService(dbFileRepository);
  }

  @Test
  void testStore(@Mock DBFile dbFile, @Mock MultipartFile multipartFile) {
    when(dbFileRepository.save(any())).thenReturn(dbFile);
    when(multipartFile.getOriginalFilename()).thenReturn("name");

    DBFile resultDbFile = dbFileStorageService.storeFile(multipartFile);

    assertEquals(dbFile, resultDbFile);
  }

  @Test
  void testStoreException(@Mock DBFile dbFile, @Mock MultipartFile multipartFile)
      throws IOException {
    when(multipartFile.getBytes()).thenThrow(new IOException());
    when(multipartFile.getOriginalFilename()).thenReturn("name");

    assertThrows(
        InvalidStorageException.class, () -> dbFileStorageService.storeFile(multipartFile));
  }

  @Test
  void testGetFile(@Mock DBFile dbFile) {
    when(dbFileRepository.findById(anyLong())).thenReturn(Optional.of(dbFile));

    DBFile result = dbFileStorageService.getFile(1L);

    assertEquals(result, dbFile);
  }

  @Test
  void testGetFileNoFile() {
    when(dbFileRepository.findById(anyLong())).thenReturn(Optional.empty());
    assertThrows(StorageFileNotFoundException.class, () -> dbFileStorageService.getFile(1L));
  }
}
