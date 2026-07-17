package edu.cit.erag.souvenirpos.sale.controller;

import edu.cit.erag.souvenirpos.sale.dto.SaleCreateRequest;
import edu.cit.erag.souvenirpos.sale.dto.SaleResponse;
import edu.cit.erag.souvenirpos.sale.dto.SaleSummaryResponse;
import edu.cit.erag.souvenirpos.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SaleResponse createSale(@Valid @RequestBody SaleCreateRequest request) {
        return new SaleResponse(saleService.createSale(request));
    }

    // FR-012/FR-013: list sales, optionally filtered by a single ?date= or a ?from=&to= range.
    // All params are optional and backward-compatible with the original single-date filter.
    @GetMapping
    public List<SaleResponse> listSales(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return saleService.listSales(date, from, to).stream().map(SaleResponse::new).toList();
    }

    // Dashboard aggregates (FR-015). Declared before /{id} so the literal path wins.
    @GetMapping("/summary")
    public SaleSummaryResponse summary() {
        return saleService.summary();
    }

    @GetMapping("/{id}")
    public SaleResponse getSale(@PathVariable Long id) {
        return new SaleResponse(saleService.getSale(id));
    }
}
