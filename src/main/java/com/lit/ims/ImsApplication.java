package com.lit.ims;

<<<<<<< HEAD
//import com.lit.ims.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
=======

import org.springframework.boot.CommandLineRunner;
import com.lit.ims.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
>>>>>>> 7c625a90dbb0f87e3b8203b13749a1ae1ead3c9b
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity

public class ImsApplication implements CommandLineRunner {
//public class ImsApplication {

    @Autowired
<<<<<<< HEAD
//    private UserService userService;
=======
    private UserService userService;
>>>>>>> 7c625a90dbb0f87e3b8203b13749a1ae1ead3c9b

    public static void main(String[] args) {
        SpringApplication.run(ImsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        userService.createOwnerIfNotExists();
    }

}
