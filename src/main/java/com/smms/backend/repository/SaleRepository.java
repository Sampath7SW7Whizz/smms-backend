package com.smms.backend.repository;

import com.smms.backend.model.Sale;
import com.smms.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    /**
     * Find all sales for a specific user
     */
    List<Sale> findByUser(User user);

    @Query("SELECT DISTINCT s FROM Sale s LEFT JOIN FETCH s.items WHERE s.user = :user ORDER BY s.date DESC")
    List<Sale> findByUserWithItemsOrderByDateDesc(@Param("user") User user);

    /**
     * Find sales for a user within a date range
     * Used for daily/weekly/monthly reports
     */
    List<Sale> findByUserAndDateBetween(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Get the most recent sale for a user (for sequential bill numbering)
     * Orders by ID descending and returns the first result
     */
    Optional<Sale> findTopByUserOrderByIdDesc(User user);

    List<Sale> findTop5ByUserOrderByDateDesc(User user);

    /**
     * Optional: Find sale by transaction ID and user
     * Useful for searching specific invoices
     */
    Optional<Sale> findByUserAndTransactionId(User user, String transactionId);

    /**
     * Used to make offline-queued sale submissions idempotent: a retried
     * sync with the same clientRequestId returns the sale already saved
     * on the first attempt instead of creating a duplicate.
     */
    Optional<Sale> findByUserAndClientRequestId(User user, String clientRequestId);

    /**
     * Optional: Get total sales count for a user
     */
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * Optional: Get total revenue for a user
     */
    @Query("SELECT SUM(s.grandTotal) FROM Sale s WHERE s.user = :user")
    Double getTotalRevenueByUser(@Param("user") User user);
}
