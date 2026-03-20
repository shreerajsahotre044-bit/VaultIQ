package com.VaultIQ.Controller;


import com.VaultIQ.Entity.ProfileEntity;
import com.VaultIQ.Services.DashboardService;
import com.VaultIQ.Services.ProfileServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1.0/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private ProfileServices profileServices;

    @GetMapping
    public ResponseEntity<Map<String,Object>> getDashboardData(){

        ProfileEntity profile = profileServices.getCurrentProfile();

        Map<String,Object> dashboardData =
                dashboardService.getDashboardData(profile.getId());

        return ResponseEntity.ok(dashboardData);
    }
}
