package com.hugailei.graduation.corpus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author HU Gailei
 * @date 2018/8/20
 * <p>
 *     description: 应用程序启动类
 * </p>
 **/
@SpringBootApplication
@EnableConfigurationProperties
@EntityScan({"com.hugailei.graduation.corpus.domain"})
@EnableJpaRepositories(basePackages = {"com.hugailei.graduation.corpus.dao"})
@EnableElasticsearchRepositories(basePackages = {"com.hugailei.graduation.corpus.elasticsearch"})
@EnableScheduling
@EnableCaching
@EnableHystrix
public class CorpusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CorpusApplication.class, args);
    }
}
