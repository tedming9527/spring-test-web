package org.example.springtestweb.redis.dto;

public record UserCache (
  Long id,
  String username,
  String name,
  String email
) {}
