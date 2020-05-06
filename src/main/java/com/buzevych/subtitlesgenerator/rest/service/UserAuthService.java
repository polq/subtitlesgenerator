package com.buzevych.subtitlesgenerator.rest.service;

import com.buzevych.subtitlesgenerator.rest.model.User;
import com.buzevych.subtitlesgenerator.rest.model.UserDTO;
import com.buzevych.subtitlesgenerator.rest.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Class that is used to handle user authorization requests sent from a controller, has method to
 * register a new user and to find a user by username.
 */
@Service
@Slf4j
public class UserAuthService {

  private UserRepository userRepository;
  private PasswordEncoder encoder;

  @Autowired
  public UserAuthService(UserRepository userRepository, PasswordEncoder encoder) {
    this.userRepository = userRepository;
    this.encoder = encoder;
  }

  public User register(UserDTO user) {
    User alreadyRegisteredUser = userRepository.findByUsername(user.getUsername());
    if (alreadyRegisteredUser != null) {
      log.error("User with the username {} already exists", user.getUsername());

      throw new IllegalArgumentException(
          "User with the '" + user.getUsername() + "' username is already registered");
    }
    User newUser = new User();
    newUser.setUsername(user.getUsername());
    newUser.setPassword(encoder.encode(user.getPassword()));
    newUser = userRepository.save(newUser);

    log.info("User {} successfully saved", newUser.getUsername());

    return newUser;
  }

  public User findByUsername(String username) {
    User foundUser = userRepository.findByUsername(username);
    if (foundUser == null) {
      log.warn("User with the username {} was not found", username);
      throw new UsernameNotFoundException("User with the " + username + " not found");
    }
    log.info("User with the username {} has been successfully found.", username);
    return foundUser;
  }

  public User newRequest(String ipAddress) {
    User foundUser = userRepository.findByIpAddress(ipAddress);
    if (foundUser != null) {
      foundUser.setLatestRequest(LocalDateTime.now());
      userRepository.save(foundUser);
    }
    return foundUser;
  }
}
