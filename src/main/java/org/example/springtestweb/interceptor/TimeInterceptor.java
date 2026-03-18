package org.example.springtestweb.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class TimeInterceptor implements HandlerInterceptor {
  private final ThreadLocal<LocalTime> threadLocalStart = new ThreadLocal<>();
  private final ThreadLocal<LocalTime> threadLocalEnd = new ThreadLocal<>();
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    LocalTime start = LocalTime.now();
    threadLocalStart.set(start);
    log.info("preHandle start: {}", start.format(formatter));
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    LocalTime end = LocalTime.now();
    threadLocalEnd.set(end);
    log.info("postHandle end: {}", end.format(formatter));
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    LocalTime start = threadLocalStart.get();
    LocalTime end = threadLocalEnd.get();
    log.info("afterCompletion: duration {}", Duration.between(start, end));
  }
}
