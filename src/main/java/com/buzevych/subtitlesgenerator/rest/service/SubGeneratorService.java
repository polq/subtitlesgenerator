package com.buzevych.subtitlesgenerator.rest.service;

import com.buzevych.subtitlesgenerator.files.exceptions.InvalidFFmpegException;
import com.buzevych.subtitlesgenerator.files.manipulation.FFmpegService;
import com.buzevych.subtitlesgenerator.files.model.DBFile;
import com.buzevych.subtitlesgenerator.files.storage.service.GoogleCloudStorageService;
import com.buzevych.subtitlesgenerator.speech.text.MyWordInfo;
import com.buzevych.subtitlesgenerator.speech.text.SpeechFileProducer;
import com.buzevych.subtitlesgenerator.speech.voice.VoiceRecognizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** Service class that is used to generate subtitles for a */
@Service
@Slf4j
public class SubGeneratorService {

  @Value("${ffmpeg.audio.format}")
  private String audioFormat;

  @Value("${ffmpeg.audio.format.bitrate}")
  private Long audioBitrate;

  @Value("${ffmpeg.video.format}")
  private String videoFormat;

  private FFmpegService fFmpegService;
  private SpeechFileProducer speechFileProducer;
  private VoiceRecognizer voiceRecognizer;
  private GoogleCloudStorageService googleCloudStorageService;

  @Autowired
  public SubGeneratorService(
      FFmpegService fFmpegService,
      SpeechFileProducer speechFileProducer,
      VoiceRecognizer voiceRecognizer,
      GoogleCloudStorageService googleCloudStorageService) {
    this.fFmpegService = fFmpegService;
    this.speechFileProducer = speechFileProducer;
    this.voiceRecognizer = voiceRecognizer;
    this.googleCloudStorageService = googleCloudStorageService;
  }

  /**
   * Method that takes source File and generates subtitles for it.
   *
   * @param sourceFile
   * @return {@link DBFile} already modified file
   * @throws IOException
   */
  public DBFile createSubs(DBFile sourceFile) throws IOException {
    File source = File.createTempFile("temp", sourceFile.getFileName());
    Files.write(Paths.get(source.getPath()), sourceFile.getData());
    File audioFile = extractAudio(source);
    File subFile = createSubFile(audioFile);
    File resultFile = combinedFiles(source, subFile);
    return DBFile.of(resultFile);
  }

  private File extractAudio(File sourceFile) {
    try {
      File outPutAudioFile = fFmpegService.execute(sourceFile, audioFormat);
      log.info("Auido has been successfully extracted from {}", sourceFile.getName());
      return outPutAudioFile;
    } catch (IOException exp) {
      throw new InvalidFFmpegException(
          "There was an error while converting " + sourceFile.getName() + " file into audio format",
          exp);
    }
  }

  private File combinedFiles(File sourceVideo, File sourceSubs) {
    String[] subArgsList = new String[] {"-i", sourceSubs.getAbsolutePath(), "-c:s", "mov_text"};

    try {
      File combinedFile =
          fFmpegService.execute(sourceVideo, videoFormat, new String[0], subArgsList);
      log.info("Files has been successfully combined");
      return combinedFile;
    } catch (IOException exp) {
      throw new InvalidFFmpegException("There was an error while combining files ", exp);
    }
  }

  private File createSubFile(File audioSource) {
    long fileSize = audioSource.length();
    try {
      List<MyWordInfo> recognisedList;
      if (isShorterThan(fileSize, 60)) {
        String audioFile = googleCloudStorageService.store(audioSource);
        recognisedList = voiceRecognizer.recognizeFromGS(audioFile, "en-Us");
      } else {
        recognisedList = voiceRecognizer.recognizeFromLocalFile(audioSource, "en-US");
      }
      File subFile = speechFileProducer.produce(recognisedList);
      log.info("Sub file has been successfully created");
      return subFile;
    } catch (IOException | InterruptedException | ExecutionException e) {
      throw new IllegalArgumentException(e);
    }
  }

  private boolean isShorterThan(long fileSize, long benchmark) {
    long maxFileSize = audioBitrate * 1000 * benchmark / 8;
    return fileSize > maxFileSize;
  }
}
