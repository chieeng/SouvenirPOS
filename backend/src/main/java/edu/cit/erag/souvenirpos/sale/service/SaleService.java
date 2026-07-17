package edu.cit.erag.souvenirpos.sale.service;

import edu.cit.erag.souvenirpos.sale.dto.SaleCreateRequest;
import edu.cit.erag.souvenirpos.sale.dto.SaleItemRequest;
import edu.cit.erag.souvenirpos.sale.dto.SaleSummaryResponse;
import edu.cit.erag.souvenirpos.shared.domain.Category;
import edu.cit.erag.souvenirpos.shared.domain.Sale;
import edu.cit.erag.souvenirpos.shared.domain.SaleItem;
import edu.cit.erag.souvenirpos.shared.domain.User;
import edu.cit.erag.souvenirpos.category.repository.CategoryRepository;
import edu.cit.erag.souvenirpos.sale.repository.SaleRepository;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
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

    /**
     * Sales history (FR-012), optionally narrowed (FR-013). A {@code from}/{@code to} range
     * takes precedence; a single {@code date} filters one day; no filter returns all sales.
     * An open-ended range (only one bound) collapses to that single day, and a reversed
     * range is swapped, so the query is always bounded and deterministic.
     */
    @Transactional(readOnly = true)
    public List<Sale> listSales(LocalDate date, LocalDate from, LocalDate to) {
        if (from != null || to != null) {
            LocalDate startDate = (from != null) ? from : to;
            LocalDate endDate = (to != null) ? to : from;
            if (startDate.isAfter(endDate)) {
                LocalDate swap = startDate;
                startDate = endDate;
                endDate = swap;
            }
            return saleRepository.findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(
                    startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        }
        if (date != null) {
            return saleRepository.findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(
                    date.atStartOfDay(), date.atTime(LocalTime.MAX));
        }
        return saleRepository.findAllByOrderBySaleDateTimeDesc();
    }

    @Transactional(readOnly = true)
    public Sale getSale(Long id) {
        // findWithItemsById eager-fetches items/cashier so the controller can map to a
        // SaleResponse after this transaction closes (open-in-view=false) without a
        // LazyInitializationException.
        return saleRepository.findWithItemsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + id));
    }

    /**
     * Aggregates sales for the dashboard (FR-015): today's total, this calendar
     * week's total (Monday–today), and a per-day breakdown of the last 7 days.
     * One bounded query covers all three windows since Monday is at most 6 days
     * back; totals are summed with BigDecimal to preserve two-decimal peso money.
     */
    @Transactional(readOnly = true)
    public SaleSummaryResponse summary() {
        LocalDate today = LocalDate.now();
        LocalDate windowStart = today.minusDays(6); // last 7 days inclusive
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<Sale> sales = saleRepository.findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(
                windowStart.atStartOfDay(), today.atTime(LocalTime.MAX));

        BigDecimal todayTotal = BigDecimal.ZERO;
        long todayCount = 0;
        BigDecimal weekTotal = BigDecimal.ZERO;
        long weekCount = 0;

        // Pre-seed a bucket per day so days with no sales still appear as zero.
        List<SaleSummaryResponse.DailyBucket> daily = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            BigDecimal dayTotal = BigDecimal.ZERO;
            long dayCount = 0;
            for (Sale sale : sales) {
                if (sale.getSaleDateTime().toLocalDate().equals(day)) {
                    dayTotal = dayTotal.add(sale.getTotalAmount());
                    dayCount++;
                }
            }
            daily.add(new SaleSummaryResponse.DailyBucket(day, dayTotal, dayCount));

            if (day.equals(today)) {
                todayTotal = dayTotal;
                todayCount = dayCount;
            }
            if (!day.isBefore(weekStart)) {
                weekTotal = weekTotal.add(dayTotal);
                weekCount += dayCount;
            }
        }

        return new SaleSummaryResponse(todayTotal, todayCount, weekTotal, weekCount, daily);
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
