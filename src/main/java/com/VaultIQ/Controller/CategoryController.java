package com.VaultIQ.Controller;

import com.VaultIQ.DTO.CategoryDTO;
import com.VaultIQ.Services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1.0/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDTO>saveCategory(@RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategory = categoryService.saveCategory(categoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>>getCategories(){
        List<CategoryDTO>categories = categoryService.getCategoriesForCurrentUser();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<CategoryDTO>>getCategoriesByTypeForCurrentUser(@PathVariable String type){
        List<CategoryDTO>list = categoryService.getCategoriesByTypeForCurrentUser(type);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO>updateCategory(@PathVariable Long categoryId , @RequestBody CategoryDTO categoryDTO){
        CategoryDTO updatedCategory = categoryService.updateCategory(categoryId,categoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }
}
