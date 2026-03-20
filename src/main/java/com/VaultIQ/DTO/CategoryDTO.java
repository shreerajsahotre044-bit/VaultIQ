package com.VaultIQ.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class CategoryDTO implements Serializable {
    private Long id;
    private Long profileId;
    private String name;
    private String icon;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
