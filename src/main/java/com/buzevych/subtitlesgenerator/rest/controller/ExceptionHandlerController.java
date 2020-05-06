package com.buzevych.subtitlesgenerator.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

  /**
   * Method that is used to handle Internal Exceptions and output them in JSON format
   *
   * @param cause represents exception that was caught
   * @return
   */
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity processRuntimeException(Throwable cause) {
    log.error(Arrays.toString(cause.getStackTrace()));
    Map<String, String> map = new HashMap<>();
    map.put("error", cause.getMessage());
    return ResponseEntity.badRequest().body(map);
  }
}
