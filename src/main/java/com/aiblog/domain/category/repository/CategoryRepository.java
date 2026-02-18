package com.aiblog.domain.category.repository;

import com.aiblog.domain.category.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  List<Category> findAllByOrderByDisplayOrder();

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, Long id);

  @Query("SELECT COUNT(p) > 0 FROM Post p WHERE p.category.id = :categoryId")
  boolean existsPostByCategoryId(@Param("categoryId") Long categoryId);
}
