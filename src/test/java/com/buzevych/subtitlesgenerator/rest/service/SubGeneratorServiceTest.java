package com.buzevych.subtitlesgenerator.rest.service;

import com.buzevych.subtitlesgenerator.files.exceptions.InvalidFFmpegException;
import com.buzevych.subtitlesgenerator.files.manipulation.FFmpegService;
import com.buzevych.subtitlesgenerator.files.model.DBFile;
import com.buzevych.subtitlesgenerator.files.storage.service.GoogleCloudStorageService;
import com.buzevych.subtitlesgenerator.speech.text.MyWordInfo;
import com.buzevych.subtitlesgenerator.speech.text.SpeechFileProducer;
import com.buzevych.subtitlesgenerator.speech.voice.VoiceRecognizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubGeneratorServiceTest {

  @Mock private FFmpegService fFmpegService;
  @Mock private SpeechFileProducer speechFileProducer;
  @Mock private VoiceRecognizer voiceRecognizer;
  @Mock private GoogleCloudStorageService googleCloudStorageService;

  SubGeneratorService subGeneratorService;

  @BeforeEach
  void init() {
    subGeneratorService =
        new SubGeneratorService(
            fFmpegService, speechFileProducer, voiceRecognizer, googleCloudStorageService);
    ReflectionTestUtils.setField(subGeneratorService, "audioFormat", ".flac");
    ReflectionTestUtils.setField(subGeneratorService, "videoFormat", ".mp4");
    ReflectionTestUtils.setField(subGeneratorService, "audioBitrate", 120L);
  }

  @Test
  void testGenerateSubsUnderOneMinute(
      @Mock File audioFile, @Mock List<MyWordInfo> myWordInfoList, @Mock File subFile)
      throws IOException {
    Path file = Files.createTempFile("temp", "temp.txt");
    DBFile dbFile = DBFile.of(file.toFile());
    when(fFmpegService.execute(any(), anyString())).thenReturn(audioFile);
    when(voiceRecognizer.recognizeFromLocalFile(eq(audioFile), anyString()))
        .thenReturn(myWordInfoList);
    when(speechFileProducer.produce(myWordInfoList)).thenReturn(subFile);
    when(fFmpegService.execute(any(), anyString(), any(), any())).thenReturn(file.toFile());

    DBFile resultFile = subGeneratorService.createSubs(dbFile);

    assertEquals(DBFile.of(file.toFile()), resultFile);
  }

  @Test
  void testGenerateSubs(
      @Mock File audioFile, @Mock List<MyWordInfo> myWordInfoList, @Mock File subFile)
      throws IOException, ExecutionException, InterruptedException {
    Path file = Files.createTempFile("temp", "temp.txt");
    DBFile dbFile = DBFile.of(file.toFile());

    when(fFmpegService.execute(any(), anyString())).thenReturn(audioFile);
    when(audioFile.length()).thenReturn(1000000L);
    when(googleCloudStorageService.store(eq(audioFile))).thenReturn("fileName");
    when(voiceRecognizer.recognizeFromGS(eq("fileName"), anyString())).thenReturn(myWordInfoList);
    when(speechFileProducer.produce(myWordInfoList)).thenReturn(subFile);
    when(fFmpegService.execute(any(), anyString(), any(), any())).thenReturn(file.toFile());

    DBFile resultFile = subGeneratorService.createSubs(dbFile);

    assertEquals(DBFile.of(file.toFile()), resultFile);
  }

  @Test
  void testAudioException() throws IOException {
    Path file = Files.createTempFile("temp", "temp.txt");
    DBFile dbFile = DBFile.of(file.toFile());

    when(fFmpegService.execute(any(), anyString())).thenThrow(new IOException());

    assertThrows(InvalidFFmpegException.class, () -> subGeneratorService.createSubs(dbFile));
  }

  @Test
  void testCombinedFileException(
      @Mock File audioFile, @Mock List<MyWordInfo> myWordInfoList, @Mock File subFile)
      throws IOException, ExecutionException, InterruptedException {
    Path file = Files.createTempFile("temp", "temp.txt");
    DBFile dbFile = DBFile.of(file.toFile());

    when(fFmpegService.execute(any(), anyString())).thenReturn(audioFile);
    when(audioFile.length()).thenReturn(1000000L);
    when(googleCloudStorageService.store(eq(audioFile))).thenReturn("fileName");
    when(voiceRecognizer.recognizeFromGS(eq("fileName"), anyString())).thenReturn(myWordInfoList);
    when(speechFileProducer.produce(myWordInfoList)).thenReturn(subFile);
    when(fFmpegService.execute(any(), anyString(), any(), any())).thenThrow(new IOException());

    assertThrows(InvalidFFmpegException.class, () -> subGeneratorService.createSubs(dbFile));
  }
}
