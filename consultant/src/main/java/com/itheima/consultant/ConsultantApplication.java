package com.itheima.consultant;

import com.google.errorprone.annotations.Var;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.temporal.ValueRange;

@SpringBootApplication
public class ConsultantApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsultantApplication.class, args);
    }
}
