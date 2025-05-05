package org.almagestauth;


import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class AlmagestAuthServer {

    @PostConstruct
    public void init() {
    // 애플리케이션 루트 디렉토리 하위에 log 폴더 생성
        File logDir = new File("log");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(AlmagestAuthServer.class, args);
    }
}