package com.aiblog.domain.category.entity;

import com.aiblog.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(length = 255)
  private String description;

  @Column(nullable = false)
  private int displayOrder;

  @Builder
  public Category(String name, String slug, String description, int displayOrder) {
    this.name = name;
    this.slug = slug;
    this.description = description;
    this.displayOrder = displayOrder;
  }

  public void update(String name, String slug, String description,
      int displayOrder) {
    this.name = name;
    this.slug = slug;
    this.description = description;
    this.displayOrder = displayOrder;
  }
}
