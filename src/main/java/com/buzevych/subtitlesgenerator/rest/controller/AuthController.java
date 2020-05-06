package com.buzevych.subtitlesgenerator.rest.controller;

import com.buzevych.subtitlesgenerator.rest.model.User;
import com.buzevych.subtitlesgenerator.rest.model.UserDTO;
import com.buzevych.subtitlesgenerator.rest.security.JwtTokenProvider;
import com.buzevych.subtitlesgenerator.rest.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** Controller class that is used to authorized a user. Has method to login and register. */
@RestController
@RequestMapping("/auth")
public class AuthController {

  private JwtTokenProvider tokenProvider;
  private UserAuthService userAuthService;
  private AuthenticationManager authenticationManager;

  @Autowired
  public AuthController(
      JwtTokenProvider tokenProvider,
      UserAuthService userAuthService,
      AuthenticationManager authenticationManager) {
    this.tokenProvider = tokenProvider;
    this.userAuthService = userAuthService;
    this.authenticationManager = authenticationManager;
  }

  /**
   * Method that is used to register a new user with the given credentials in a following JSON
   * format: { "username": "admin", "password": "pass" }
   *
   * @param userDTO defines input
   * @return 200 code with the username, if user was successfully registered.
   */
  @PostMapping("/registration")
  public ResponseEntity registration(@RequestBody UserDTO userDTO) {
    User registeredUser = userAuthService.register(userDTO);
    Map<Object, Object> response = new HashMap<>();
    response.put("username", registeredUser.getUsername());
    return ResponseEntity.ok(response);
  }

  /**
   * Method that is used to obtain JWT token for a already registered user. Input should be in the
   * following JSON format: { "username": "admin", "password": "pass" }
   *
   * @param userDTO defines input
   * @return a JSON string in the following format
   * { "username": "user",
   *   "token": "eyJhbjE1ODUyMTc0NDksIs"
   * }
   * Given token will be used to make further requests to an REST application.
   */
  @PostMapping("/login")
  public ResponseEntity login(@RequestBody UserDTO userDTO) {
    String username = userDTO.getUsername();
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(username, userDTO.getPassword()));

    String token = tokenProvider.createToken(username);
    Map<Object, Object> response = new HashMap<>();
    response.put("username", username);
    response.put("token", token);
    return ResponseEntity.ok(response);
  }
}
