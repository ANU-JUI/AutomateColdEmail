package com.login.firstproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class Emailservice {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${resume.drive.link:#{null}}") // Default to null if not set
    private String resumeDriveLink;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String toEmail, String toName, String subject, String body, boolean attachResume) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, false);

            // Only attach resume if requested AND resume link is configured
            if (attachResume && resumeDriveLink != null && !resumeDriveLink.isEmpty()) {
                try {
                    byte[] resumeData = downloadResumeFromDrive();
                    if (resumeData != null) {
                        helper.addAttachment("Resume.pdf", new ByteArrayResource(resumeData));
                        System.out.println("Resume attached successfully");
                    } else {
                        System.out.println("Could not download resume from Drive");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to attach resume: " + e.getMessage());
                    // Continue sending email without resume
                }
            }

            mailSender.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to: " + toEmail, e);
        }
    }

    private byte[] downloadResumeFromDrive() {
        try {
            if (resumeDriveLink == null || resumeDriveLink.isEmpty()) {
                return null;
            }
            
            if (resumeDriveLink.contains("/file/d/")) {
                String fileId = resumeDriveLink.split("/file/d/")[1].split("/")[0];
                String directDownloadLink = "https://drive.google.com/uc?export=download&id=" + fileId;
                return restTemplate.getForObject(directDownloadLink, byte[].class);
            }
            return restTemplate.getForObject(resumeDriveLink, byte[].class);
        } catch (Exception e) {
            System.err.println("Error downloading resume from Drive: " + e.getMessage());
            return null;
        }
    }

    public void sendBulkEmails(java.util.List<EmailRequest> emailRequests) {
        for (EmailRequest request : emailRequests) {
            sendEmail(request.getToEmail(), request.getToName(), 
                     request.getSubject(), request.getBody(), request.isAttachResume());
        }
    }
}