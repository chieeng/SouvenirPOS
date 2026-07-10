package edu.cit.erag.souvenirpos.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class SaleCreateRequest {

    // BR-002: a sale must contain at least one line item.
    @NotEmpty(message = "A sale must have at least one item")
    @Valid
    private List<SaleItemRequest> items;

    @NotNull
    private BigDecimal paymentAmount;

    public List<SaleItemRequest> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequest> items) {
        this.items = items;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }
}
