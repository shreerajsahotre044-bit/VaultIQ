package com.VaultIQ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
//@EnableCaching
public class  VaultIqApplication {

	public static void main(String[] args) {
		SpringApplication.run(VaultIqApplication.class, args);
	}

}
