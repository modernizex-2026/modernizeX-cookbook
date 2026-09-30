package com.generated.orion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot web application for orion CICS system. Scans all program modules and app-runtime via
 * classpath.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.generated.orion", "com.appruntime"})
public class CicsApplication {
    public static void main(String[] args) {
        System.setProperty("cobol.numeric.lenient", "true");
        System.setProperty("cobol.subscript.lenient", "true");
        SpringApplication.run(CicsApplication.class, args);
    }
}
