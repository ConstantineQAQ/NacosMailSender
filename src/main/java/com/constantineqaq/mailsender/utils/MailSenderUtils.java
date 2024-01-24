package com.constantineqaq.mailsender.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author wangyaning33
 */
@Component
public class MailSenderUtils {

    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.recipient}")
    private String recipient;

    @Value("${spring.mail.username}")
    private String sender;

    public void sendMail(String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(content);
        javaMailSender.send(message);
    }
}
