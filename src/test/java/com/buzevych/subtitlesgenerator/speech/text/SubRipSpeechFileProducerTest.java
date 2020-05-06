package com.buzevych.subtitlesgenerator.speech.text;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubRipSpeechFileProducerTest {
  SubRipSpeechFileProducer subRipTextFile;

  @BeforeEach
  void init() {
    subRipTextFile = new SubRipSpeechFileProducer();
  }

  @Test
  void testToSRTFormat() {
    Duration duration = Duration.ofSeconds(3, 900000000);
    String toSRT = subRipTextFile.toSRTFormat(duration);
    System.out.println(subRipTextFile.toSRTFormat(Duration.ofSeconds(3)));
    System.out.println(toSRT);
    Assertions.assertEquals("00:00:03,901", toSRT);
  }

  @Test
  void testProduce() throws IOException {
    List<MyWordInfo> wordInfoList = new ArrayList<>();
    wordInfoList.add(
        new MyWordInfo(new StringBuilder("one"), Duration.ofSeconds(0), Duration.ofSeconds(1)));
    wordInfoList.add(
        new MyWordInfo(new StringBuilder("two"), Duration.ofSeconds(1), Duration.ofSeconds(2)));
    wordInfoList.add(
        new MyWordInfo(new StringBuilder("three"), Duration.ofSeconds(3), Duration.ofSeconds(4)));

    File file = subRipTextFile.produce(wordInfoList);
    file.deleteOnExit();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder stringBuilder = new StringBuilder();
    String tempString;
    while ((tempString = reader.readLine()) != null) {
      stringBuilder.append("\n").append(tempString);
    }

    Assertions.assertEquals("one two three", stringBuilder.toString().split("\n")[3]);
    Assertions.assertTrue(file.delete());
  }
}
