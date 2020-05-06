package com.buzevych.subtitlesgenerator.files.manipulation;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * Main file manipulation service that is used to edit, extract, combine files based on a FFMPEG.
 */
@Slf4j
@Service
public class FFmpegService {

  @Value("${ffmpeg.location}")
  private String pathToFFmpeg;

  private FFmpeg ffmpeg;

  @PostConstruct
  void init() throws IOException {
    ffmpeg = new FFmpeg(pathToFFmpeg);
    log.info("FFmpeg with the path {} has been initialized", pathToFFmpeg);
  }

  /**
   * Executes ffmpeg command with the given parameters.
   *
   * @param inputFile files that is to be edited
   * @param toFormat format of output file
   * @param inArgs arguments that will be applied to input file
   * @param outArgs arguments that will be applied to output file
   * @return modified {@link File} in the required format
   * @throws IOException
   */
  public File execute(File inputFile, String toFormat, String[] inArgs, String[] outArgs)
      throws IOException {
    File outPutFile = File.createTempFile("tempFile", "." + toFormat);

    FFmpegBuilder builder =
        new FFmpegBuilder().setInput(inputFile.getAbsolutePath()).overrideOutputFiles(true);

    if (inArgs.length != 0) {
      builder.addExtraArgs(inArgs);
    }

    if (outArgs.length != 0) {
      builder.addOutput(outPutFile.getAbsolutePath()).addExtraArgs(outArgs);
    } else {
      builder.addOutput(outPutFile.getAbsolutePath());
    }
    FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

    executor.createJob(builder).run();

    log.info("File {} has been modified to {} extension" + inputFile.getName(), toFormat);
    return outPutFile;
  }

  /**
   * Short execution method for ffmpeg.
   *
   * @param inputFile files that is to be edited
   * @param toFormat format of output file
   * @return modified {@link File} in the required format
   * @throws IOException
   */
  public File execute(File inputFile, String toFormat) throws IOException {
    return execute(inputFile, toFormat, new String[0], new String[0]);
  }
}
