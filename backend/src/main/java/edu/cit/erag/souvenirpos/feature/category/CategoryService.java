package edu.cit.erag.souvenirpos.feature.category;

import edu.cit.erag.souvenirpos.feature.category.CategoryCreateRequest;
import edu.cit.erag.souvenirpos.shared.domain.Category;
import edu.cit.erag.souvenirpos.feature.category.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Category createCategory(CategoryCreateRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException("Category already exists");
        }
        return categoryRepository.save(new Category(name));
    }
}
