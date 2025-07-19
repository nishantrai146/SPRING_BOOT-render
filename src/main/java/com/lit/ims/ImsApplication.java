package com.lit.ims;


import org.springframework.boot.CommandLineRunner;
import com.lit.ims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
@EnableJpaAuditing
public class ImsApplication implements CommandLineRunner {
//public class ImsApplication {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(ImsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        userService.createOwnerIfNotExists();
    }

}
