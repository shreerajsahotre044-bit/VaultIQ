package com.VaultIQ.Controller;

import com.VaultIQ.DTO.AuthDTO;
import com.VaultIQ.DTO.ProfileDTO;
import com.VaultIQ.Services.ProfileServices;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1.0")
public class ProfileController {

    @Autowired
    private ProfileServices profileServices;

    @PostMapping("/register")
    public ResponseEntity<ProfileDTO> registerProfile(@RequestBody ProfileDTO profileDTO){
        ProfileDTO registeredProfile = profileServices.registerProfile(profileDTO);
        return ResponseEntity.ok(registeredProfile);
    }

    @GetMapping("/activate")
    public ResponseEntity<String>activateProfile(@RequestParam String token){
        boolean isActivated = profileServices.activateProfile(token);
        if(isActivated){
            return ResponseEntity.ok("Profile Activated Successfully");
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token is not found");
        }
    }

    @PostMapping("/login")
    public  ResponseEntity<Map<String , Object>>login(@RequestBody AuthDTO authDTO){
        try {
            if(!(profileServices.isAccountActive(authDTO.getEmail()))){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "Message"  , "Account is not active please activate the account"
                ));
            }
            Map<String , Object> response = profileServices.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/test")
    public String test(){
        return "You have Access of it ";
    }
}
