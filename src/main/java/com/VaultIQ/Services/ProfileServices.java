package com.VaultIQ.Services;

import com.VaultIQ.DTO.AuthDTO;
import com.VaultIQ.DTO.ProfileDTO;
import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Repository.ProfileRepository;
import com.VaultIQ.Util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ProfileServices {
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${vaultiq.activation.url}")
    private String activationURL;

    public ProfileDTO registerProfile(ProfileDTO profileDTO) {

        if(profileRepository.findByEmail(profileDTO.getEmail()).isPresent()){
            throw new RuntimeException("Email already registered");
        }


        ProfileEntity newProfile = toEntity(profileDTO);
        newProfile.setActivationToken(UUID.randomUUID().toString());
        newProfile = profileRepository.save(newProfile);

        // Activation link
        String activationLink = activationURL+"/api/v1.0/activate?token=" + newProfile.getActivationToken();

        // Subject
        String subject = "Activate Your VaultIQ Account";

        // Styled HTML Body
        String body = """
        <div style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 30px;">
            <div style="max-width: 600px; margin: auto; background-color: #ffffff; padding: 25px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <h2 style="color: #2E8B57; text-align: center;">Welcome to VaultIQ!</h2>
                <p>Hi <strong>%s</strong>,</p>
                <p>Thank you for registering with <strong>VaultIQ – Your Smart Money Manager</strong>.</p>
                <p>To activate your account and start using our features, please click the button below:</p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" style="background-color: #2E8B57; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">Activate Account</a>
                </div>
                <p>If you didn’t request this, please ignore this email.</p>
                <hr style="border: none; border-top: 1px solid #ccc;">
                <p style="font-size: 0.9em; color: #777;">Regards,<br>VaultIQ Team</p>
            </div>
        </div>
        """.formatted(newProfile.getFullname(), activationLink);

        emailService.sendMail(newProfile.getEmail(), subject, body);
        log.info("Email Successfully Delivered");
        return toDTO(newProfile);
    }


    public ProfileEntity toEntity(ProfileDTO profileDTO){
        return ProfileEntity.builder()
                .fullname(profileDTO.getFullname())
                .email(profileDTO.getEmail())
                .password(passwordEncoder.encode(profileDTO.getPassword()))
                .profileImageURL(profileDTO.getProfileImageURL())
                .phoneNumber(profileDTO.getPhoneNumber())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity profileEntity){
        return ProfileDTO.builder()
                .id(profileEntity.getId())
                .fullname(profileEntity.getFullname())
                .email(profileEntity.getEmail())
//                .password(profileEntity.getPassword())
                .profileImageURL(profileEntity.getProfileImageURL())
                .phoneNumber(profileEntity.getPhoneNumber())
                .createdAt(profileEntity.getCreatedAt())
                .updatedAt(profileEntity.getUpdatedAt())
                .build();
    }


        public boolean activateProfile(String activationToken){
            return profileRepository.findByActivationToken(activationToken)
                    .map(profileEntity -> {
                        profileEntity.setIsActive(true);
                        profileRepository.save(profileEntity);
                        return true;
                    }).orElse(false);

        }

        public boolean isAccountActive(String email){
            return profileRepository.findByEmail(email)
                    .map(ProfileEntity::getIsActive)
                    .orElse(false);
        }

        public ProfileEntity getCurrentProfile(){
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            return profileRepository.findByEmail(email)
                    .orElseThrow(()-> new UsernameNotFoundException("User not found with email " + email));
        }

        public ProfileDTO getPublicProfile(String email){
            ProfileEntity currentUser = null;
            if(email == null){
                currentUser = getCurrentProfile();
            }else{
                currentUser = profileRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Profile not found with username :  " + email));
            }

            return toDTO(currentUser);
        }

    public Map<String, Object> authenticateAndGenerateToken(AuthDTO authDTO) {
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail() , authDTO.getPassword()));
            //generate token
            String token = jwtUtil.generateToken(authDTO.getEmail());
            return Map.of(
                    "token" , token,
                    "user" , getPublicProfile(authDTO.getEmail())
            );
        }catch (Exception e){
            throw new RuntimeException("invalid email or password");
        }
    }
}
