package com.buzevych.subtitlesgenerator.speech.voice;

import com.buzevych.subtitlesgenerator.speech.text.MyWordInfo;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Scope("prototype")
@Slf4j
public class VoiceRecognizer {

  /**
   * Used to get List of words from a {@link Resource} file. The input audio file should be of: WAV
   * or FLAC formats. FLAC format has better recognition value, as it is loseless format. Only
   * supports audio files less than 1 minute (Google APi constraint)
   *
   * @param resourceFile WAV or FLAC audio file
   * @param language See the list of accepted languages -
   *     https://cloud.google.com/speech-to-text/docs/languages
   * @return {@link List} of recognised {@link MyWordInfo}
   * @throws IOException
   */
  public List<MyWordInfo> recognizeFromLocalFile(File resourceFile, String language)
      throws IOException {
    String fileName = resourceFile.getName();
    List<WordInfo> recognizedWords = new ArrayList<>();
    try (SpeechClient speechClient = SpeechClient.create()) {

      Path path = resourceFile.toPath();
      byte[] data = Files.readAllBytes(path);
      ByteString audioBytes = ByteString.copyFrom(data);

      // Builds the sync recognize request
      RecognitionConfig config =
          RecognitionConfig.newBuilder()
              .setLanguageCode(language)
              .setAudioChannelCount(2)
              .setEnableWordTimeOffsets(true)
              .setEnableAutomaticPunctuation(true)
              .build();

      RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();

      // Performs speech recognition on the audio file
      RecognizeResponse response = speechClient.recognize(config, audio);
      List<SpeechRecognitionResult> results = response.getResultsList();

      for (SpeechRecognitionResult result : results) {
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        recognizedWords.addAll(alternative.getWordsList());
      }
    }
    log.info(
        "Words from audio file '{}' with '{}' language has been received. Recognised {} words",
        fileName,
        language,
        recognizedWords.size());

    return recognizedWords.stream().map(MyWordInfo::new).collect(Collectors.toList());
  }

  /**
   * Used to get List of words from a Google Cloud Storage file. The input file should be of FLAC or
   * WAV formats. Supports files that are grater than 1 minute long.
   *
   * @param gcsUri Uri to a WAV, FLAC file on Google Cloud Storage.
   * @param language See the list of accepted languages -
   *     https://cloud.google.com/speech-to-text/docs/languages
   * @return {@link List} of recognised {@link MyWordInfo}
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public List<MyWordInfo> recognizeFromGS(String gcsUri, String language)
      throws IOException, InterruptedException, ExecutionException {
    List<WordInfo> recognizedWords = new ArrayList<>();
    try (SpeechClient speech = SpeechClient.create()) {

      RecognitionConfig config =
          RecognitionConfig.newBuilder()
              .setLanguageCode(language)
              .setAudioChannelCount(2)
              .setEnableWordTimeOffsets(true)
              .setEnableAutomaticPunctuation(true)
              .build();
      RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

      // Use non-blocking call for getting file transcription
      OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
          speech.longRunningRecognizeAsync(config, audio);

      List<SpeechRecognitionResult> results = response.get().getResultsList();

      for (SpeechRecognitionResult result : results) {
        SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
        recognizedWords.addAll(alternative.getWordsList());
      }
    }

    log.info(
        "Words from Google storage URI '{}' with '{}' language has been received. Recognised {} words",
        gcsUri,
        language,
        recognizedWords.size());

    return recognizedWords.stream().map(MyWordInfo::new).collect(Collectors.toList());
  }
}
