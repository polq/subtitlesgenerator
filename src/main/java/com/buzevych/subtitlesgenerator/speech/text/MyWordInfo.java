package com.buzevych.subtitlesgenerator.speech.text;

import com.google.cloud.speech.v1.WordInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@Data
@AllArgsConstructor
public class MyWordInfo {

  private StringBuilder word;
  private Duration startTime;
  private Duration endTime;

  public MyWordInfo(WordInfo wordInfo) {
    this.word = new StringBuilder(wordInfo.getWord());
    this.startTime =
        Duration.ofSeconds(
            wordInfo.getStartTime().getSeconds(), wordInfo.getStartTime().getNanos());
    this.endTime =
        Duration.ofSeconds(wordInfo.getEndTime().getSeconds(), wordInfo.getEndTime().getNanos());
  }

  void appendNewWordInfo(MyWordInfo myWordInfo) {
    this.word.append(" ").append(myWordInfo.getWord().toString());
    this.endTime = myWordInfo.endTime;
  }
}
