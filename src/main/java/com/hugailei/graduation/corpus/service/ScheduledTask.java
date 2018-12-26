package com.hugailei.graduation.corpus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author HU Gailei
 * @date 2018/12/26
 * <p>
 * description: 定时任务
 * </p>
 **/
@Component
@Slf4j
public class ScheduledTask {
    /**
     * 每隔三分钟清空缓存
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    @CacheEvict(value = {"corpus", "student"}, allEntries=true)
    public void removeAllCache(){
        log.info("removeAllCache | delete all cache..");
    }
}
