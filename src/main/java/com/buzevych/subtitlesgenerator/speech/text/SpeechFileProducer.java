package com.buzevych.subtitlesgenerator.speech.text;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SpeechFileProducer {
    /**
     *  Generates sub file from a list of words
     * @param words represents list of recognised words
     * @return subtitles file
     * @throws IOException
     */
    File produce(List<MyWordInfo> words) throws IOException;
}
