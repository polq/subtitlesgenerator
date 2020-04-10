package com.buzevych.subtitlesgenerator.speech.text;

import com.google.cloud.speech.v1.WordInfo;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Scope("prototype")
@Slf4j
public class SubRipSpeechFileProducer implements SpeechFileProducer {

  private static final String FILE_EXTENSION = ".str";
  private static final String TEMP_FILE_LOCATION = "./temp/files/";
  private static final int MAX_SUB_ROW_LENGTH = 40;

  @PostConstruct
  private void init() {
    boolean created = new File(TEMP_FILE_LOCATION).mkdirs();
    log.info("Temp directory for temporarily storing files has been created - {}", created);
  }

  @Override
  public Resource produce(List<MyWordInfo> words) throws IOException {
    String fullFileName = TEMP_FILE_LOCATION + "subtitles_" + UUID.randomUUID() + FILE_EXTENSION;
    words = groupWords(words);
    words = groupRows(words);
    try (FileWriter fileWriter = new FileWriter(fullFileName)) {
      BufferedWriter writer = new BufferedWriter(fileWriter);
      for (int i = 0; i < words.size(); i++) {
        MyWordInfo wordInfo = words.get(i);
        writer.write(i + 1 + "");
        writer.newLine();
        String from = toSRTFormat(wordInfo.getStartTime());
        String to = toSRTFormat(wordInfo.getEndTime());
        writer.write(from + " --> " + to);
        writer.newLine();
        writer.write(wordInfo.getWord().toString());
        writer.newLine();
        writer.newLine();
      }
    }
    log.info(
        "SRT file '{}' has been created and populated with {} words", fullFileName, words.size());
    return new FileUrlResource(fullFileName);
  }

  private List<MyWordInfo> groupWords(List<MyWordInfo> wordsList) {
    List<MyWordInfo> combinedWordList = new ArrayList<>();
    MyWordInfo myWordInfo = wordsList.get(0);
    for (int i = 1; i < wordsList.size(); i++) {
      MyWordInfo nextWord = wordsList.get(i);

      int currentWordLength = myWordInfo.getWord().length();
      int nextWordCombinedLength = currentWordLength + nextWord.getWord().length();

      if (nextWordCombinedLength < MAX_SUB_ROW_LENGTH
          && !(nextWord.getStartTime().minusSeconds(1).compareTo(myWordInfo.getEndTime()) > 0
              || (myWordInfo.getWord().length() > MAX_SUB_ROW_LENGTH - 10
                  && myWordInfo.getWord().toString().endsWith(".")))) {
        myWordInfo.appendNewWordInfo(nextWord);
      } else {
        combinedWordList.add(myWordInfo);
        myWordInfo = nextWord;
      }
    }
    combinedWordList.add(myWordInfo);
    log.info("Words from wordsList are successfully grouped");
    return combinedWordList;
  }

  private List<MyWordInfo> groupRows(List<MyWordInfo> wordsList) {
    List<MyWordInfo> resultList = new ArrayList<>();
    MyWordInfo wordInfo = new MyWordInfo();
    for (int i = 1; i < wordsList.size(); i++) {
      MyWordInfo nextWord = wordsList.get(i);
      if (!wordInfo.getWord().toString().contains("\n")
          || nextWord.getStartTime().minusSeconds(1).compareTo(wordInfo.getEndTime()) < 0) {
        wordInfo.getWord().append("\n");
        wordInfo.appendNewWordInfo(nextWord);
      }
      resultList.add(wordInfo);
      wordInfo = nextWord;
    }
    resultList.add(wordInfo);
    log.info("Words from wordsList are combined into rows");
    return resultList;
  }

  @VisibleForTesting
  String toSRTFormat(Duration duration) {
    LocalTime time = LocalTime.of(0, 0, (int) duration.getSeconds(), duration.getNano());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    time.format(formatter);
    return time.toString().replace(".", ",");
  }
}
