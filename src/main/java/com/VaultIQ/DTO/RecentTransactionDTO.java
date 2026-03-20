package com.VaultIQ.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RecentTransactionDTO implements Serializable {
    private Long id;
    private Long profileId;
    private String icon;
    private String name;
    private BigDecimal amount;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type;
}
