package com.buzevych.subtitlesgenerator.rest.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Class that is used to configure Spring Security Adapter to used custom Filter based on JWT token
 * provider. Has inner class that is used to add additional filter in a filter chain, which will get
 * token from HTTP request header, check it for validity and set authentication made from it to
 * SecurityContextHolder.
 */
public class JwtConfigurer
    extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

  private JwtTokenProvider jwtTokenProvider;
  private HandlerExceptionResolver exceptionResolver;

  @Autowired
  public JwtConfigurer(
      JwtTokenProvider jwtTokenProvider, HandlerExceptionResolver exceptionResolver) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.exceptionResolver = exceptionResolver;
  }

  @Override
  public void configure(HttpSecurity builder) {
    JwtTokenFilter jwtTokenFilter = new JwtTokenFilter(jwtTokenProvider, exceptionResolver);
    builder.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
  }

  public static class JwtTokenFilter extends GenericFilterBean {

    JwtTokenProvider jwtTokenProvider;
    HandlerExceptionResolver exceptionResolver;

    public JwtTokenFilter(
        JwtTokenProvider jwtTokenProvider, HandlerExceptionResolver exceptionResolver) {
      this.jwtTokenProvider = jwtTokenProvider;
      this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void doFilter(
        ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
      String token = jwtTokenProvider.resolveToken((HttpServletRequest) servletRequest);
      Authentication authentication;

      if (token != null && jwtTokenProvider.validateToken(token)) {
        authentication = jwtTokenProvider.getAuthentication(token);
      } else {
        String ipAddress = servletRequest.getRemoteAddr();
        try {
          authentication = jwtTokenProvider.getIPAuthentication(ipAddress);
        } catch (IllegalArgumentException e) {
          exceptionResolver.resolveException(
              (HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, null, e);
          return;
        }
      }

      if (authentication != null) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      filterChain.doFilter(servletRequest, servletResponse);
    }
  }
}
