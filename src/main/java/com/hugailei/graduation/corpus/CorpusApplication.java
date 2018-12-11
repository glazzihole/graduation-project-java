package com.hugailei.graduation.corpus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan({"com.hugailei.graduation.corpus.domain"})
@EnableJpaRepositories(basePackages = {"com.hugailei.graduation.corpus.dao"})
@EnableElasticsearchRepositories(basePackages = {"com.hugailei.graduation.corpus.elasticsearch"})
public class CorpusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorpusApplication.class, args);
    }
}
