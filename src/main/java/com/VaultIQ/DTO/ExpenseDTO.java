package com.VaultIQ.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDTO implements Serializable {
    private Long id;
    private String name;
    private String icon;
    private String categoryName;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDateTime date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
