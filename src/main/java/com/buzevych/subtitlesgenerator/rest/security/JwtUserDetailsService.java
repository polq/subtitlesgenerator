package com.buzevych.subtitlesgenerator.rest.security;

import com.buzevych.subtitlesgenerator.rest.model.User;
import com.buzevych.subtitlesgenerator.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {

  @Autowired UserRepository userRepository;

  @Value("#{new Long('${jwt.token.request.restriction.perminute}')}")
  private long unAuthorizedRequestRestriction;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User with the " + username + " was not found");
    }

    List<GrantedAuthority> userAuthorities =
        Collections.singletonList(new SimpleGrantedAuthority("USER"));

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), userAuthorities);
  }

  public UserDetails loadUserByIP(String ipAddress) {
    User user = userRepository.findByIpAddress(ipAddress);

    if (user != null) {
      LocalDateTime latestRequest = user.getLatestRequest();
      if (latestRequest.plusMinutes(unAuthorizedRequestRestriction).isAfter(LocalDateTime.now())) {
        throw new IllegalArgumentException(
            "Unauthorized users can only make "
                + unAuthorizedRequestRestriction
                + " per minute. If you wish to make more request, please wait from your latest request "
                + latestRequest.toString()
                + " or login to make unlimited amount of requests");
      }
    } else {
      user = new User(ipAddress);
    }
    userRepository.save(user);

    List<GrantedAuthority> userAuthorities =
        Collections.singletonList(new SimpleGrantedAuthority("USER"));
    return new org.springframework.security.core.userdetails.User(ipAddress, "", userAuthorities);
  }
}
