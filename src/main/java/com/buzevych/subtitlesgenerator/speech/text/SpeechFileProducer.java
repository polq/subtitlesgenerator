package com.buzevych.subtitlesgenerator.speech.text;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public interface SpeechFileProducer {
    /**
     *  Generates
     * @param words
     * @return
     * @throws IOException
     */
    Resource produce(List<MyWordInfo> words) throws IOException;
}
