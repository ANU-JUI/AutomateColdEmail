package com.login.firstproject;

import com.login.firstproject.EmailRequest;
import com.login.firstproject.Emailservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {
    "https://automatecoldemail-frontend.onrender.com",
    "http://localhost:3000"
})
public class EmailController {

    @Autowired
    private Emailservice emailService;

    @PostMapping("/send-single")
    public ResponseEntity<String> sendSingleEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(
                emailRequest.getToEmail(),
                emailRequest.getToName(),
                emailRequest.getSubject(),
                emailRequest.getBody(),
                emailRequest.isAttachResume()
            );
            return ResponseEntity.ok("Email sent successfully to " + emailRequest.getToEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<String> sendBulkEmails(@RequestBody List<EmailRequest> emailRequests) {
        try {
            emailService.sendBulkEmails(emailRequests);
            return ResponseEntity.ok("Bulk emails sent successfully to " + emailRequests.size() + " recipients");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send bulk emails: " + e.getMessage());
        }
    }
}