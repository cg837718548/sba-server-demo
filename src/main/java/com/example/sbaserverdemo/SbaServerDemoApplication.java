package com.example.sbaserverdemo;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer
public class SbaServerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbaServerDemoApplication.class, args);
    }

}
