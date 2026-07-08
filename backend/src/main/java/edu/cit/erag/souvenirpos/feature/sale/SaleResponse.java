package edu.cit.erag.souvenirpos.feature.sale;

import edu.cit.erag.souvenirpos.shared.domain.Sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SaleResponse {

    private Long id;
    private LocalDateTime saleDateTime;
    private Long cashierId;
    private String cashierName;
    private BigDecimal totalAmount;
    private BigDecimal paymentAmount;
    private BigDecimal changeAmount;
    private List<SaleItemResponse> items;

    public SaleResponse(Sale sale) {
        this.id = sale.getId();
        this.saleDateTime = sale.getSaleDateTime();
        this.cashierId = sale.getCashier().getId();
        this.cashierName = sale.getCashier().getName();
        this.totalAmount = sale.getTotalAmount();
        this.paymentAmount = sale.getPaymentAmount();
        this.changeAmount = sale.getChangeAmount();
        this.items = sale.getItems().stream().map(SaleItemResponse::new).toList();
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getSaleDateTime() {
        return saleDateTime;
    }

    public Long getCashierId() {
        return cashierId;
    }

    public String getCashierName() {
        return cashierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public List<SaleItemResponse> getItems() {
        return items;
    }
}
