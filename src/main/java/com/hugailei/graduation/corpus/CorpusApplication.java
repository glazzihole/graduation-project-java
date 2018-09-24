package com.hugailei.graduation.corpus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"com.hugailei.graduation.corpus.domain"})
public class CorpusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorpusApplication.class, args);
    }
}
