package edu.cit.erag.souvenirpos.sale.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class SaleItemRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    private BigDecimal unitPrice;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
