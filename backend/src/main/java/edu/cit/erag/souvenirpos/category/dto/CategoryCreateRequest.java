package edu.cit.erag.souvenirpos.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryCreateRequest {

    @NotBlank
    @Size(max = 60)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
