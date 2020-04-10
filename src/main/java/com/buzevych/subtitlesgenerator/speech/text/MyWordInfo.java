package com.buzevych.subtitlesgenerator.speech.text;

import com.google.cloud.speech.v1.WordInfo;
import lombok.Data;

import java.time.Duration;

@Data
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

  MyWordInfo() {
    this.word = new StringBuilder();
    this.startTime = Duration.ofSeconds(0);
    this.endTime = Duration.ofSeconds(0);
  }

  void appendNewWordInfo(MyWordInfo myWordInfo) {
    this.word.append(" ").append(myWordInfo.getWord().toString());
    this.endTime = myWordInfo.endTime;
  }
}
