package edu.cit.erag.souvenirpos.repository;

import edu.cit.erag.souvenirpos.shared.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findAllByOrderBySaleDateTimeDesc();

    List<Sale> findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(LocalDateTime start, LocalDateTime end);
}
