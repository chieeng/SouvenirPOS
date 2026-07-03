package edu.cit.erag.souvenirpos.service;

import edu.cit.erag.souvenirpos.dto.SaleCreateRequest;
import edu.cit.erag.souvenirpos.dto.SaleItemRequest;
import edu.cit.erag.souvenirpos.entity.Category;
import edu.cit.erag.souvenirpos.entity.Sale;
import edu.cit.erag.souvenirpos.entity.SaleItem;
import edu.cit.erag.souvenirpos.entity.User;
import edu.cit.erag.souvenirpos.repository.CategoryRepository;
import edu.cit.erag.souvenirpos.repository.SaleRepository;
import edu.cit.erag.souvenirpos.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public SaleService(SaleRepository saleRepository,
                       CategoryRepository categoryRepository,
                       UserRepository userRepository) {
        this.saleRepository = saleRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Sale createSale(SaleCreateRequest request) {
        // BR-004: link the sale to the currently authenticated cashier.
        User cashier = currentUser();

        Sale sale = new Sale();
        sale.setCashier(cashier);
        sale.setSaleDateTime(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (SaleItemRequest line : request.getItems()) {
            Category category = categoryRepository.findById(line.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + line.getCategoryId()));

            // FR-008: subtotals and total are computed on the server, never trusted from the client.
            BigDecimal unitPrice = line.getUnitPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(subtotal);

            sale.addItem(new SaleItem(category, line.getQuantity(), unitPrice, subtotal));
        }

        // BR-003: payment must be at least the total.
        BigDecimal payment = request.getPaymentAmount();
        if (payment.compareTo(total) < 0) {
            throw new IllegalArgumentException("Payment amount must be greater than or equal to the total");
        }

        sale.setTotalAmount(total);
        sale.setPaymentAmount(payment);
        sale.setChangeAmount(payment.subtract(total)); // FR-009

        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public List<Sale> listSales(LocalDate date) {
        if (date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            return saleRepository.findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(start, end);
        }
        return saleRepository.findAllByOrderBySaleDateTimeDesc();
    }

    @Transactional(readOnly = true)
    public Sale getSale(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + id));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalArgumentException("No authenticated user for this sale");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }
}
