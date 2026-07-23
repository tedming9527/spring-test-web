package org.example.springtestweb.category.bo;

import lombok.Data;

@Data
public class CategoryNameBo {
  private Long id;
  private String name;
  private String image;

  public CategoryNameBo(Long id, String name, String iconUrl) {
    this.id = id;
    this.name = name;
    this.image = iconUrl;
  }
}
