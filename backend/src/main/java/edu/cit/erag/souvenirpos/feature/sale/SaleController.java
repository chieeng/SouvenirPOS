package edu.cit.erag.souvenirpos.feature.sale;

import edu.cit.erag.souvenirpos.feature.sale.SaleCreateRequest;
import edu.cit.erag.souvenirpos.feature.sale.SaleResponse;
import edu.cit.erag.souvenirpos.feature.sale.SaleService;
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

    @GetMapping
    public List<SaleResponse> listSales(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return saleService.listSales(date).stream().map(SaleResponse::new).toList();
    }

    @GetMapping("/{id}")
    public SaleResponse getSale(@PathVariable Long id) {
        return new SaleResponse(saleService.getSale(id));
    }
}
