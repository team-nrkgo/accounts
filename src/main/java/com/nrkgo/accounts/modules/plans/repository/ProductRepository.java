package com.nrkgo.accounts.modules.plans.repository;

import com.nrkgo.accounts.modules.plans.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlugAndStatus(String slug, Integer status);

    Optional<Product> findByProductCodeAndStatus(Integer productCode, Integer status);

    boolean existsByProductCode(Integer productCode);
}
