package edu.cit.erag.souvenirpos.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aggregated sales figures for the dashboard (FR-015). Holds today's and this
 * week's totals plus a per-day breakdown of the last 7 days, so the web and
 * mobile dashboards render identical numbers from one source.
 */
public class SaleSummaryResponse {

    private final BigDecimal todayTotal;
    private final long todayCount;
    private final BigDecimal weekTotal;
    private final long weekCount;
    private final List<DailyBucket> daily;

    public SaleSummaryResponse(BigDecimal todayTotal, long todayCount,
                               BigDecimal weekTotal, long weekCount,
                               List<DailyBucket> daily) {
        this.todayTotal = todayTotal;
        this.todayCount = todayCount;
        this.weekTotal = weekTotal;
        this.weekCount = weekCount;
        this.daily = daily;
    }

    public BigDecimal getTodayTotal() {
        return todayTotal;
    }

    public long getTodayCount() {
        return todayCount;
    }

    public BigDecimal getWeekTotal() {
        return weekTotal;
    }

    public long getWeekCount() {
        return weekCount;
    }

    public List<DailyBucket> getDaily() {
        return daily;
    }

    /** One day's rolled-up sales, used for the 7-day breakdown chart. */
    public static class DailyBucket {

        private final LocalDate date;
        private final BigDecimal total;
        private final long count;

        public DailyBucket(LocalDate date, BigDecimal total, long count) {
            this.date = date;
            this.total = total;
            this.count = count;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public long getCount() {
            return count;
        }
    }
}
