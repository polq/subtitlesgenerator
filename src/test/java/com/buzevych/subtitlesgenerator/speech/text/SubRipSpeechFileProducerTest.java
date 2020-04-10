package com.buzevych.subtitlesgenerator.speech.text;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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

      assertEquals("00:00:03,900", toSRT);
  }
}
