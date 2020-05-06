package com.buzevych.subtitlesgenerator.files.storage.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleCloudStorageServiceTest {

  private GoogleCloudStorageService storageService;

  @Mock private Storage storage;

  @BeforeEach
  void init() {
    storageService = new GoogleCloudStorageService(storage);
    ReflectionTestUtils.setField(storageService,"bucketName","bucket");
    ReflectionTestUtils.setField(storageService,"fileNamePrefix","gs://");
  }

  @Test
  void testStore() throws IOException {
    when(storage.create(any(), any(byte[].class))).thenReturn(null);
    File file = new File("test.txt");
    assertTrue(file.createNewFile());
    file.deleteOnExit();

    String fileURI = storageService.store(file);

    assertEquals("gs://bucket/test.txt", fileURI );
  }

  @Test
  void testRead(@Mock Blob blob, @Mock File file) throws IOException {
    when(storage.get((BlobId) any())).thenReturn(blob);
    File get = storageService.get("filename.txt");

    assertTrue(get.getName().endsWith("filename.txt"));
  }
}
