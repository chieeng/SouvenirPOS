package edu.cit.erag.souvenirpos.dto;

import edu.cit.erag.souvenirpos.entity.SaleItem;

import java.math.BigDecimal;

public class SaleItemResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    public SaleItemResponse(SaleItem item) {
        this.id = item.getId();
        this.categoryId = item.getCategory().getId();
        this.categoryName = item.getCategory().getName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.subtotal = item.getSubtotal();
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
