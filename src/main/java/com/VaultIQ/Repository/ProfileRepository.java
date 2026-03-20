package com.VaultIQ.Repository;

import com.VaultIQ.Entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


public interface ProfileRepository extends JpaRepository<ProfileEntity , String> {
    Optional<ProfileEntity>findByEmail(String email);
    Optional<ProfileEntity>findByActivationToken(String Token);
}
