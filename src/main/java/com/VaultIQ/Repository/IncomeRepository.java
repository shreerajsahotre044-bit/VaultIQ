package com.VaultIQ.Repository;

import com.VaultIQ.Entity.ExpenseEntity;
import com.VaultIQ.Entity.IncomeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IncomeRepository extends JpaRepository<IncomeEntity , Long> {
    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);

    @Query("SELECT SUM(e.amount) FROM IncomeEntity e WHERE e.profile.id = :profileId")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
            Long profileId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String keyword,
            Sort sort
    );


    List<IncomeEntity>findByProfileIdAndDateBetween(Long profileId , LocalDateTime startDate , LocalDateTime endDate);


}
