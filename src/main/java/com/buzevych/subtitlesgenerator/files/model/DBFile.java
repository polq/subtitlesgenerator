package com.buzevych.subtitlesgenerator.files.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
public class DBFile {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "Id")
  private Long Id;

  @Column(name = "fileName")
  private String fileName;

  @Column(name = "fileType")
  private String fileType;

  @Lob
  @Column(name = "data")
  private byte[] data;

  public static DBFile of(MultipartFile multipartFile) throws IOException {
    DBFile newDBFile = new DBFile();
    newDBFile.fileName =
        StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    newDBFile.fileType = multipartFile.getContentType();
    newDBFile.data = multipartFile.getBytes();
    return newDBFile;
  }

  public static DBFile of(File file) throws IOException {
    DBFile newDBFile = new DBFile();
    newDBFile.fileName = file.getName();
    newDBFile.fileType = Files.probeContentType(file.toPath());
    newDBFile.data = Files.readAllBytes(file.toPath());
    return newDBFile;
  }
}
