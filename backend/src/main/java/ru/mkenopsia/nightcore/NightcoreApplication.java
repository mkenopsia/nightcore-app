package ru.mkenopsia.nightcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class NightcoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NightcoreApplication.class, args);
    }

}
