package com.project.partnerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class PartnerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerServiceApplication.class, args);

    }

}