package edu.cit.erag.souvenirpos.category.controller;

import edu.cit.erag.souvenirpos.category.dto.CategoryCreateRequest;
import edu.cit.erag.souvenirpos.category.dto.CategoryResponse;
import edu.cit.erag.souvenirpos.category.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> listCategories() {
        return categoryService.listCategories().stream().map(CategoryResponse::new).toList();
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        return new CategoryResponse(categoryService.createCategory(request));
    }
}
