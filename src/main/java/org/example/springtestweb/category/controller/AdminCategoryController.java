package org.example.springtestweb.category.controller;

import org.example.springtestweb.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {
  @Autowired
  private CategoryService categoryService;
  @PutMapping("/{id}/name")
  public ResponseEntity<Void> updateName(@PathVariable Long id, @RequestParam String name) {
    boolean updated = categoryService.updateName(id, name);
    if (updated) return ResponseEntity.noContent().build();
    return ResponseEntity.notFound().build();
  }
}
