package com.VaultIQ.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullname;
    @Column(unique = true)
    private String email;
    private String password;
    private String phoneNumber;
    private String profileImageURL;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive = false;
    private String activationToken;

    @PrePersist
    public void prePresist(){
        if(this.isActive == null){
            isActive = false;
        }
    }


}
