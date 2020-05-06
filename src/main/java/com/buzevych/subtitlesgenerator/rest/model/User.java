package com.buzevych.subtitlesgenerator.rest.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

  @Id
  @Column(name = "ID")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long Id;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "IP_Address")
  private String ipAddress;

  @Column(name = "latest_request")
  private LocalDateTime latestRequest;

  public User(String ipAddress) {
    this.ipAddress = ipAddress;
    this.latestRequest = LocalDateTime.now();
  }
}
