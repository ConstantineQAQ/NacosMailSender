package com.constantineqaq.mailsender.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.constantineqaq.mailsender.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author wangyaning33
 */
@Slf4j
@EnableScheduling
@Configuration
public class ScheduleTaskConfiguration {

    @Resource
    private ConfigService configService;
    @Resource
    private RedisUtils redisUtils;

    @Schedules(
            @Scheduled(cron = "*/10 * * * * *")
    )
    public void getConfig() {
        try {
            String before = configService.getConfig(NacosConfiguration.DATA_ID, NacosConfiguration.GROUP_ID, 5000L);
            Properties properties = new Properties();
            properties.load(new StringReader(before));
            if (properties.isEmpty()) {
                log.error("properties is empty");
                return;
            }
            properties.forEach((k,v)-> {
                String propertyKey = (String) k;
                String propertyValue = (String) v;
                log.info("key: {}, value: {}", propertyKey, propertyValue);
                if (redisUtils.hget("NacosConfig",propertyKey) == null) {
                    redisUtils.hset("NacosConfig", propertyKey, propertyValue);
                    redisUtils.expire("NacosConfig", 600);
                    log.info("设置缓存成功");
                }
            });
        } catch (Exception e) {
            log.error("get config error", e);
        }
    }
}
