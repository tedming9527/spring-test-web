package org.example.springtestweb.controller;

import org.example.springtestweb.model.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class ParamController {
  @GetMapping("/api/noannotation")
  public User noAnnotation(User user) {
    return user;
  }

  @GetMapping("/api/requestparam")
  public User requestParam(@RequestParam String name, @RequestParam int age) {
    User user = new User();
    user.setName(name);
    user.setAge(age);
    return user;
  }

  @GetMapping("/api/pathvariable/{name}/{age}")
  public User pathVariable(@PathVariable String  name, @PathVariable int age) {
    User user = new User();
    user.setName(name);
    user.setAge(age);
    return user;
  }
  @PostMapping("/api/requestbody")
  public User requestBody(@RequestBody User user) {
    return user;
  }
}
