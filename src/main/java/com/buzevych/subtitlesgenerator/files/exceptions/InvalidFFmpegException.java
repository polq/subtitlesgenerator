package com.buzevych.subtitlesgenerator.files.exceptions;

public class InvalidFFmpegException extends RuntimeException {
  public InvalidFFmpegException(String message, Throwable cause) {
    super(message, cause);
  }
}
