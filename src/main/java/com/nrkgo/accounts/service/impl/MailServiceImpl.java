package com.nrkgo.accounts.service.impl;

import com.nrkgo.accounts.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, true); // Default to HTML
    }

    @Async
    @Override
    public void sendEmail(String to, String subject, String body, boolean isHtml) {
        log.info("Sending email to: {}", to);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(body, isHtml);

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            // Consider throwing a custom exception if critical
        }
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String filePath) throws MessagingException, IOException {
        log.info("Sending email with attachment to: {}", to);
        
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        FileSystemResource file = new FileSystemResource(new File(filePath));
        helper.addAttachment(file.getFilename(), file);

        javaMailSender.send(message);
        log.info("Email with attachment sent successfully to: {}", to);
    }

    @Async
    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachmentData, String fileName) throws MessagingException {
        log.info("Sending email with byte attachment to: {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setTo(to);
        helper.setFrom(fromEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        helper.addAttachment(fileName, new ByteArrayResource(attachmentData));

        javaMailSender.send(message);
        log.info("Email with byte attachment sent successfully to: {}", to);
    }
}
