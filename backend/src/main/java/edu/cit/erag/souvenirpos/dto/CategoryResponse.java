package edu.cit.erag.souvenirpos.dto;

import edu.cit.erag.souvenirpos.shared.domain.Category;

public class CategoryResponse {

    private Long id;
    private String name;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
