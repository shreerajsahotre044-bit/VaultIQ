package com.VaultIQ.Services;

import com.VaultIQ.DTO.CategoryDTO;
import com.VaultIQ.Entity.CategoryEntity;
import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private ProfileServices profileServices;
    @Autowired
    private CategoryRepository categoryRepository;

    private CategoryEntity convertToEntity(CategoryDTO categoryDTO , ProfileEntity profile){
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO convertToDto(CategoryEntity entity){
        return  CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .updatedAt(entity.getUpdatedAt())
                .createdAt(entity.getCreatedAt())
                .type(entity.getType())
                .build();
    }


    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        ProfileEntity profile = profileServices.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profile.getId())){
            throw  new ResponseStatusException(HttpStatus.CONFLICT , "Category with this name is already exist");
        }

        CategoryEntity newCategory = convertToEntity(categoryDTO , profile);
        newCategory = categoryRepository.save(newCategory);
        return convertToDto(newCategory);
    }

    public List<CategoryDTO>getCategoriesForCurrentUser(){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::convertToDto).toList();
    }

    public List<CategoryDTO>getCategoriesByTypeForCurrentUser(String type){
        ProfileEntity profile = profileServices.getCurrentProfile();
        List<CategoryEntity>entities = categoryRepository.findByTypeAndProfileId(type , profile.getId());
        return entities.stream().map(this::convertToDto).toList();
    }

    public CategoryDTO updateCategory(Long categoryId , CategoryDTO categoryDTO){
        ProfileEntity profile = profileServices.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        existingCategory.setType(categoryDTO.getType());
        existingCategory = categoryRepository.save(existingCategory);

        return convertToDto(existingCategory);
    }
}
