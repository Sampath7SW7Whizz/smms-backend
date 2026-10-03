package com.smms.backend.repository;

import com.smms.backend.model.Product;
import com.smms.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUser(User user);

    Optional<Product> findByUserAndItemCode(User user, String itemCode);

    Optional<Product> findByUserAndItemCodeIgnoreCase(User user, String itemCode);

    Optional<Product> findByUserAndId(User user, Long id);

    List<Product> findByUserAndIsDefault(User user, boolean isDefault);

    List<Product> findByIsDefault(boolean isDefault);

    List<Product> findByUserIsNullAndIsDefaultTrue();

    // Batch fetch products by multiple item codes (avoid N+1 queries)
    @Query("SELECT p FROM Product p WHERE p.user = :user AND LOWER(p.itemCode) IN :itemCodes")
    List<Product> findByUserAndItemCodeIn(@Param("user") User user, @Param("itemCodes") List<String> itemCodes);

    @Query("""
            SELECT p FROM Product p
            WHERE p.user = :user
                AND (
                    LOWER(p.itemName) LIKE LOWER(CONCAT('%', :query, '%'))
                    OR LOWER(COALESCE(p.itemCode, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                )
            """)
    List<Product> searchByUserAndQuery(@Param("user") User user, @Param("query") String query, Pageable pageable);
}