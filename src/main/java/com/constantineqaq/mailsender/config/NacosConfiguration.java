package com.constantineqaq.mailsender.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Properties;

/**
 * @author wangyaning33
 */
@Configuration
public class NacosConfiguration {
    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    public static final String DATA_ID = "WYNbysj";
    public static final String GROUP_ID = "DEFAULT_GROUP";

    @Primary
    @Bean
    public ConfigService configService() {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        try {
            return NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
