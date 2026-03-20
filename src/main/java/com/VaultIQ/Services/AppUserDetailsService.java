package com.VaultIQ.Services;

import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class AppUserDetailsService implements UserDetailsService {

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        ProfileEntity existingProfile = profileRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Profile Not Found with email : "+email));

        return User.builder()
                .username(existingProfile.getEmail())
                .password(existingProfile.getPassword())
                .authorities(Collections.emptyList())
                .build();

    }
}

