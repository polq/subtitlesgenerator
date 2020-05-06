package com.buzevych.subtitlesgenerator.rest.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Component class that has main method that defines JWT token interaction. It has methods to create
 * a token from a user username and defined role, to get token from an HTTP request header, to get
 * Authentication from a token and to validate token.
 */
@Component
@Slf4j
public class JwtTokenProvider {

  @Value("${jwt.token.start}")
  private String TOKEN_START;

  @Value("${jwt.token.http.header.value}")
  private String HTTP_HEADER_AUTHORIZATION_VALUE;

  @Value("${jwt.token.salt}")
  private String JWT_TOKEN_SALT;

  @Value("#{new Long('${jwt.token.validity.time}')}")
  private long TOKEN_VALIDITY_TIME;

  private JwtUserDetailsService userDetailsService;

  @Autowired
  public JwtTokenProvider(JwtUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  public String createToken(String username) {
    Claims claims = Jwts.claims().setSubject(username);

    Date now = new Date();
    Date validity = new Date(now.getTime() + TOKEN_VALIDITY_TIME);

    String finalToke =
        Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, JWT_TOKEN_SALT)
            .compact();
    log.info("Token {} for {} user has been successfully created", finalToke, username);
    return finalToke;
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public Authentication getIPAuthentication(String ipAddress) {
    UserDetails userDetails = userDetailsService.loadUserByIP(ipAddress);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader(HTTP_HEADER_AUTHORIZATION_VALUE);
    if (bearerToken != null && bearerToken.startsWith(TOKEN_START)) {
      String pureToken = bearerToken.substring(TOKEN_START.length());
      log.info("Token '{}' has been obtained from and HTTP request", pureToken);
      return pureToken;
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser().setSigningKey(JWT_TOKEN_SALT).parseClaimsJws(token);
      boolean valid = !claims.getBody().getExpiration().before(new Date());
      log.info("Token {} is valid - {}", token, valid);
      return valid;
    } catch (JwtException | IllegalArgumentException e) {
      throw new AuthenticationServiceException("JWT token is expired or invalid");
    }
  }

  private String getUsername(String token) {
    return Jwts.parser().setSigningKey(JWT_TOKEN_SALT).parseClaimsJws(token).getBody().getSubject();
  }
}
