package com.buzevych.subtitlesgenerator.rest.repository;

import com.buzevych.subtitlesgenerator.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByIpAddress(String ipAddress);

  User findByUsername(String username);
}
