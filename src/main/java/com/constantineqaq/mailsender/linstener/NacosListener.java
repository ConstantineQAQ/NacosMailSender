package com.constantineqaq.mailsender.linstener;

import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.constantineqaq.mailsender.config.NacosConfiguration;
import com.constantineqaq.mailsender.utils.MailSenderUtils;
import com.constantineqaq.mailsender.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.endpoint.event.RefreshEventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author wangyaning33
 */
@Component
@Slf4j
public class NacosListener implements ApplicationRunner {
    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    @Resource
    private ConfigService configService;

    @Resource
    private MailSenderUtils mailSenderUtils;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        listenerNacosConfig();
    }

    private void listenerNacosConfig() throws Exception {
        configService.getConfigAndSignListener(NacosConfiguration.DATA_ID, NacosConfiguration.GROUP_ID, 5000L, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("receive config info: {}", configInfo);
                Properties properties = new Properties();
                try {
                    properties.load(new StringReader(configInfo));
                    if (properties.isEmpty()) {
                        log.error("properties is empty");
                        return;
                    }
                    Map<String, String[]> changes = new HashMap<String, String[]>();
                    Map<Object, Object> redisConfig = redisUtils.hmget("NacosConfig");
                    // 检查更改和新增
                    properties.forEach((k, v) -> {
                        String propertyKey = (String) k;
                        String newValue = (String) v;
                        String oldValue = redisConfig.containsKey(propertyKey) ? (String) redisConfig.get(propertyKey) : null;

                        if (oldValue == null || !oldValue.equals(newValue)) {
                            redisUtils.hset("NacosConfig", propertyKey, newValue);
                            changes.put(propertyKey, new String[]{oldValue, newValue});
                        }
                    });

                    // 检查删除
                    redisConfig.forEach((k,v)-> {
                        String key = (String) k;
                        if (!properties.containsKey(key)) {
                            redisUtils.hdel("NacosConfig", key);
                            changes.put(key, new String[]{(String) v, null});
                        }
                    });

                    if (!changes.isEmpty()) {
                        String emailSubject = "Nacos Configuration Update Notification";
                        String emailContent = generateEmailContent(changes);
                        mailSenderUtils.sendMail(emailSubject, emailContent);
                        log.info("配置更改邮件发送成功");
                    }
                } catch (IOException e) {
                    log.error("load properties error", e);
                }
            }
        });
        log.info("Nacos Config Listener Start");
    }

    private String generateEmailContent(Map<String, String[]> changes) {
        String changesHtml = changes.entrySet().stream()
                .map(entry -> "<li><strong>Key:</strong> " + entry.getKey() +
                        "<br><strong>Old Value:</strong> " + (entry.getValue()[0] != null ? entry.getValue()[0] : "N/A") +
                        "<br><strong>New Value:</strong> " + entry.getValue()[1] + "</li>")
                .collect(Collectors.joining());

        return "<html><head><style>body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        .container {\n" +
                "            width: 80%;\n" +
                "            margin: 0 auto;\n" +
                "            background-color: #f8f8f8;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "        .header {\n" +
                "            background-color: #4CAF50;\n" +
                "            color: white;\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            background-color: #ddd;\n" +
                "            text-align: center;\n" +
                "            padding: 10px;\n" +
                "            font-size: 0.8em;\n" +
                "        }\n" +
                "        .content {\n" +
                "            margin-top: 20px;\n" +
                "        }</style></head><body>" +
                "<h2>Nacos Configuration Update Notification</h2>" +
                "<p>The following configuration values have been updated:</p>" +
                "<ul>" + changesHtml + "</ul>" +
                "<p>Please review these changes as they may affect your application.</p>" +
                // 插入nacos链接
                "<p><a href=\"http://" + serverAddr + "/nacos/#/configurationManagement?dataId=" +
                NacosConfiguration.DATA_ID + "&group=" + NacosConfiguration.GROUP_ID +
                "&namespace=public&serverAddr=localhost:8848\">Click here to view the configuration in Nacos</a></p>" +
                "</body></html>";
    }

}
