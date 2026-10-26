package com.rehab.system;

import com.rehab.system.config.DatabaseFallbackConfigurer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RehabSystemApplication {

    public static void main(String[] args) {
        DatabaseFallbackConfigurer.configure();
        SpringApplication.run(RehabSystemApplication.class, args);
    }
}
