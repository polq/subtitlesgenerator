package com.buzevych.subtitlesgenerator.rest.controller;

import com.buzevych.subtitlesgenerator.files.model.DBFile;
import com.buzevych.subtitlesgenerator.files.storage.service.DBFileStorageService;
import com.buzevych.subtitlesgenerator.rest.model.User;
import com.buzevych.subtitlesgenerator.rest.service.SubGeneratorService;
import com.buzevych.subtitlesgenerator.rest.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Controller that is used to get generates a subtitles for an input media file. */
@RestController
@RequestMapping("/subs")
public class SubsGeneratorController {

  SubGeneratorService subGeneratorService;
  DBFileStorageService fileStorageService;
  UserAuthService userAuthService;

  @Autowired
  public SubsGeneratorController(
      SubGeneratorService subGeneratorService,
      DBFileStorageService fileStorageService,
      UserAuthService userAuthService) {
    this.subGeneratorService = subGeneratorService;
    this.fileStorageService = fileStorageService;
    this.userAuthService = userAuthService;
  }

  /**
   * Method that is used to get {@link MultipartFile} file as a input and and return Resource
   *
   * @param file input
   * @param authentication
   * @return {@link ResponseEntity} containing file with subtitles.
   * @throws IOException
   */
  @PostMapping
  public ResponseEntity<Resource> multimedia(
      @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
    userAuthService.newRequest(authentication.getName());

    DBFile dbFile = fileStorageService.storeFile(file);
    DBFile resultFile = subGeneratorService.createSubs(dbFile);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(resultFile.getFileType()))
        .body(new ByteArrayResource(resultFile.getData()));
  }
}
