package edu.cit.erag.souvenirpos.sale.repository;

import edu.cit.erag.souvenirpos.shared.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // These finders back endpoints that map Sale -> SaleResponse in the controller, i.e. AFTER
    // the service @Transactional boundary closes. Sale.items is @OneToMany (lazy) and cashier is
    // @ManyToOne, so with spring.jpa.open-in-view=false the DTO mapping would hit a detached,
    // uninitialised `items` collection and throw LazyInitializationException (surfacing as a
    // masked 401 via the /error dispatch). Fetch both associations in the same query so mapping
    // never touches a lazy proxy; `distinct` collapses the duplicate parent rows the collection
    // join produces.
    @Query("select distinct s from Sale s left join fetch s.items join fetch s.cashier "
            + "order by s.saleDateTime desc")
    List<Sale> findAllByOrderBySaleDateTimeDesc();

    @Query("select distinct s from Sale s left join fetch s.items join fetch s.cashier "
            + "where s.saleDateTime between :start and :end order by s.saleDateTime desc")
    List<Sale> findBySaleDateTimeBetweenOrderBySaleDateTimeDesc(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select s from Sale s left join fetch s.items join fetch s.cashier where s.id = :id")
    Optional<Sale> findWithItemsById(@Param("id") Long id);
}
