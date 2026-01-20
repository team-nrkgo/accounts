package com.nrkgo.accounts.service;

import jakarta.mail.MessagingException;
import java.io.IOException;

public interface MailService {
    
    void sendEmail(String to, String subject, String body);

    void sendEmail(String to, String subject, String body, boolean isHtml);

    void sendEmailWithAttachment(String to, String subject, String body, String filePath) throws MessagingException, IOException;
    
    void sendEmailWithAttachment(String to, String subject, String body, byte[] attachmentData, String fileName) throws MessagingException;
}
