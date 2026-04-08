package com.meerkat.smashameerkat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstraps the Spring application and enables the scheduled game loop.
 */
@SpringBootApplication
@EnableScheduling
public class SmashAMeerkatApplication {

    /**
     * Starts the application and wires all Spring-managed components.
     */
    public static void main(String[] args) {
        SpringApplication.run(SmashAMeerkatApplication.class, args);
    }

}
