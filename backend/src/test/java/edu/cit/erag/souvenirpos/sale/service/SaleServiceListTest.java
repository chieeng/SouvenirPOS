package edu.cit.erag.souvenirpos.sale.service;

import edu.cit.erag.souvenirpos.category.repository.CategoryRepository;
import edu.cit.erag.souvenirpos.sale.repository.SaleRepository;
import edu.cit.erag.souvenirpos.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the FR-013 date filtering in {@link SaleService#listSales}. These use plain
 * Mockito (no Spring context or database) so the precedence and range-normalisation logic can
 * be verified in isolation.
 */
@ExtendWith(MockitoExtension.class)
class SaleServiceListTest {

    @Mock private SaleRepository saleRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;

    private SaleService service() {
        return new SaleService(saleRepository, categoryRepository, userRepository);
    }

    @Test
    void noFilterReturnsAllSales() {
        service().listSales(null, null, null);
        verify(saleRepository).findAllByOrderBySaleDateTimeDesc();
    }

    @Test
    void singleDateFiltersThatDay() {
        LocalDate day = LocalDate.of(2026, 7, 10);
        when(saleRepository.findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(day.atStartOfDay(),
                day.atTime(LocalTime.MAX))).thenReturn(List.of());

        service().listSales(day, null, null);

        verifyBounds(day.atStartOfDay(), day.atTime(LocalTime.MAX));
    }

    @Test
    void fromAndToFilterInclusiveRange() {
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 7);

        service().listSales(null, from, to);

        verifyBounds(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    @Test
    void reversedRangeIsSwapped() {
        LocalDate from = LocalDate.of(2026, 7, 7);
        LocalDate to = LocalDate.of(2026, 7, 1);

        service().listSales(null, from, to);

        // Normalised so start is the earlier date regardless of argument order.
        verifyBounds(to.atStartOfDay(), from.atTime(LocalTime.MAX));
    }

    @Test
    void fromOnlyCollapsesToSingleDay() {
        LocalDate from = LocalDate.of(2026, 7, 3);

        service().listSales(null, from, null);

        verifyBounds(from.atStartOfDay(), from.atTime(LocalTime.MAX));
    }

    @Test
    void rangeTakesPrecedenceOverSingleDate() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        LocalDate from = LocalDate.of(2026, 7, 1);
        LocalDate to = LocalDate.of(2026, 7, 7);

        service().listSales(date, from, to);

        // The from/to range wins; the single date is ignored.
        verifyBounds(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        verifyNoInteractions(categoryRepository);
    }

    private void verifyBounds(LocalDateTime expectedStart, LocalDateTime expectedEnd) {
        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(saleRepository).findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(start.capture(), end.capture());
        assertThat(start.getValue()).isEqualTo(expectedStart);
        assertThat(end.getValue()).isEqualTo(expectedEnd);
    }
}
