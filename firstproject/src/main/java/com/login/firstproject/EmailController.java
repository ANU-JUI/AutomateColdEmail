package com.login.firstproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {
    "https://automatecoldemail.netlify.app/",
    "http://localhost:3000"
})
public class EmailController {

    @Autowired
    private Emailservice emailService;
    @Autowired
    private Employeeservice employeeservice;

    @PostMapping("/send-single")
    public ResponseEntity<String> sendSingleEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(
                emailRequest.getToEmail(),
                emailRequest.getToName(),
                emailRequest.getSubject(),
                emailRequest.getBody(),
                emailRequest.getCompany(),
                emailRequest.isAttachResume(),
                emailRequest.getResumeFilename()
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

    @PostMapping("/upload-resume")
    public ResponseEntity<?> uploadResume(@RequestParam("resume") org.springframework.web.multipart.MultipartFile resume) {
        try {
            if (resume == null || resume.isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "File is empty"));
            }
            String filename = null;
            // use Employeeservice to save temp resume
            filename = employeeservice.saveTempResume(resume);
            return ResponseEntity.ok(java.util.Map.of("filename", filename));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Failed to upload resume: " + e.getMessage()));
        }
    }
}