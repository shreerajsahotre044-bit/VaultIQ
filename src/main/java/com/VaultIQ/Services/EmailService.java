package com.VaultIQ.Services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;


    public MimeMessage createMimeMessage() {
        return mailSender.createMimeMessage();
    }

    public void sendMimeMessage(MimeMessage message) {
        mailSender.send(message);
    }

    public void sendMail(String to , String subject , String body){
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = send as HTML


            System.out.println("Sending HTML email to: " + to + " from: " + fromEmail);
            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void sendMail(String to, String subject, String body, byte[] attachmentData, String attachmentFileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = HTML content

            if (attachmentData != null && attachmentFileName != null) {
                helper.addAttachment(
                        attachmentFileName,
                        () -> new ByteArrayInputStream(attachmentData)
                );
            }

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error sending email with attachment: " + e.getMessage(), e);
        }
    }

}
