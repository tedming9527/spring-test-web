package org.example.springtestweb.category.controller;

import org.example.springtestweb.category.bo.CategoryNameBo;
import org.example.springtestweb.category.service.CategoryService;
import org.example.springtestweb.category.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/interfaces/categories", "/api/merchant/categories", "/api/admin/categories"})
public class CategoryController {
  @Autowired
  private CategoryService categoryService;
  @GetMapping("/{parentId}/children")
  public List<CategoryVo> getListByParentId(@PathVariable Long parentId) throws InterruptedException {
    return categoryService.findByParentId(parentId);
  }

}
