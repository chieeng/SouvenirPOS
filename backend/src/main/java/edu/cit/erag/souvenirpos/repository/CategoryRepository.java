package edu.cit.erag.souvenirpos.repository;

import edu.cit.erag.souvenirpos.shared.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    List<Category> findAllByOrderByNameAsc();
}
