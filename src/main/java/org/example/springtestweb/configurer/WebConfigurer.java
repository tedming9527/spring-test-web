package org.example.springtestweb.configurer;

import org.example.springtestweb.interceptor.LogInterceptor;
import org.example.springtestweb.interceptor.TimeInterceptor;
import org.example.springtestweb.interceptor.AuthTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigurer implements WebMvcConfigurer {
  @Autowired
  private LogInterceptor logInterceptor;
  @Autowired
  private TimeInterceptor timeInterceptor;
  @Autowired
  private AuthTokenInterceptor authTokenInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(logInterceptor);
    registry.addInterceptor(timeInterceptor);
    registry.addInterceptor(authTokenInterceptor);
  }
}
