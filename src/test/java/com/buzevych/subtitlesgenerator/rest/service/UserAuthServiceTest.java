package com.buzevych.subtitlesgenerator.rest.service;

import com.buzevych.subtitlesgenerator.rest.model.User;
import com.buzevych.subtitlesgenerator.rest.model.UserDTO;
import com.buzevych.subtitlesgenerator.rest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

  @Mock UserRepository userRepository;

  UserAuthService userAuthService;

  @BeforeEach
  void init() {
    userAuthService = new UserAuthService(userRepository, new BCryptPasswordEncoder());
  }

  @Test
  void testRegisterAlreadyRegisterUser(@Mock User user) {
    when(userRepository.findByUsername(anyString())).thenReturn(user);
    UserDTO userDTO = new UserDTO("username", "password");
    assertThrows(IllegalArgumentException.class, () -> userAuthService.register(userDTO));
  }

  @Test
  void testRegisterNewUser(@Mock User user) {
    when(userRepository.findByUsername(anyString())).thenReturn(null);
    when(userRepository.save(any())).thenReturn(user);
    UserDTO userDTO = new UserDTO("username", "password");

    assertEquals(user, userAuthService.register(userDTO));
  }

  @Test
  void testFindByUsernameNotFound() {
    when(userRepository.findByUsername(anyString())).thenReturn(null);
    assertThrows(UsernameNotFoundException.class, () -> userAuthService.findByUsername("username"));
  }

  @Test
  void testFindByUsername(@Mock User user) {
    when(userRepository.findByUsername(anyString())).thenReturn(user);
    assertEquals(user, userAuthService.findByUsername("user"));
  }
}
