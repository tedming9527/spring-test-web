package org.example.springtestweb.context;

/**
 * 存取当前请求的 userId
 * <p> {@code REQUEST_USER_ID} 为每个线程提供了存取userId 的统一方法。
 * {@link #clear()} 方法用于在请求处理完成后清理 ThreadLocal，避免内存泄漏。
 */
public final class RequestUserContext {
  private static final ThreadLocal<String> REQUEST_USER_ID = new ThreadLocal<>();

  public static void setUserId(String userId) {
    REQUEST_USER_ID.set(userId);
  }
  public static String getUserId() {
    return REQUEST_USER_ID.get();
  }
  public static void clear() {
    REQUEST_USER_ID.remove();
  }
}