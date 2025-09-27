package com.javabruse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
        "com.javabruse.repository",
})
@EntityScan(basePackages = {
        "com.javabruse.model",
})
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}