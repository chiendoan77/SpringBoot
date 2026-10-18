package com.example.product.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.product.data"
})
@EntityScan("com.example.product.data.model")
@EnableJpaRepositories("com.example.product.data.repository")
public class ProductDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductDataApplication.class, args);
    }
}
