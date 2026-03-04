package com.login.firstproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;
import java.util.Base64;

@Service
public class Emailservice {

    @Autowired
    private com.sendgrid.SendGrid sendGrid;

    @Value("${sendgrid.from:}")
    private String fromAddress;

    @Value("${resume.drive.link:#{null}}") // Default to null if not set
    private String resumeDriveLink;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private Employeeservice employeeservice;

    public void sendEmail(String toEmail, String toName, String subject, String body, String company, boolean attachResume, String resumeFilename) {
        // replace placeholders server-side as well
        if (body != null) {
            body = body.replace("[HR_NAME]", toName == null ? "" : toName);
            if (company != null) {
                body = body.replace("[COMPANY_NAME]", company);
            }
        }
        if (subject != null) {
            subject = subject.replace("[HR_NAME]", toName == null ? "" : toName);
            if (company != null) {
                subject = subject.replace("[COMPANY_NAME]", company);
            }
        }

        Mail mail = new Mail();
        mail.setFrom(new Email(fromAddress));
        mail.setSubject(subject);
        mail.addContent(new Content("text/plain", body));

        com.sendgrid.helpers.mail.objects.Personalization personalization = new com.sendgrid.helpers.mail.objects.Personalization();
        personalization.addTo(new Email(toEmail, toName));
        mail.addPersonalization(personalization);

        if (attachResume) {
            byte[] resumeData = null;
            // priority: provided resumeFilename (uploaded), then drive link
            if (resumeFilename != null && !resumeFilename.isEmpty()) {
                try {
                    resumeData = employeeservice.getTempResumeBytes(resumeFilename);
                } catch (Exception e) {
                    System.err.println("Error reading uploaded resume: " + e.getMessage());
                }
            }
            if (resumeData == null && resumeDriveLink != null && !resumeDriveLink.isEmpty()) {
                resumeData = downloadResumeFromDrive();
            }
            if (resumeData != null) {
                Attachments attachment = new Attachments();
                attachment.setFilename("Resume.pdf");
                attachment.setType("application/pdf");
                attachment.setDisposition("attachment");
                attachment.setContent(Base64.getEncoder().encodeToString(resumeData));
                mail.addAttachments(attachment);
            }
        }

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            System.out.println("SendGrid response code: " + response.getStatusCode());
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send email via SendGrid", ex);
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
                     request.getSubject(), request.getBody(), request.getCompany(),
                     request.isAttachResume(), request.getResumeFilename());
        }
    }
}