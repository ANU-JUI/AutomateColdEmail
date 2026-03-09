package com.login.firstproject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {
    "https://automatecoldemail.netlify.app",
    "http://localhost:3000"
})
public class EmailController {

    @Autowired
    private GmailService gmailService;
    @Autowired
    private Employeeservice employeeservice;

    @PostMapping("/send-single")
    public ResponseEntity<String> sendSingleEmail(@RequestBody EmailRequest emailRequest) {
        try {
            GmailEmailResponse response = gmailService.sendEmail(
                emailRequest.getUserId(),
                emailRequest.getToEmail(),
                emailRequest.getToName(),
                emailRequest.getSubject(),
                emailRequest.getBody(),
                emailRequest.getCompany(),
                emailRequest.isAttachResume(),
                emailRequest.getResumeFilename()
            );
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                return ResponseEntity.ok("Email sent successfully to " + emailRequest.getToEmail());
            } else {
                return ResponseEntity.badRequest().body("Failed to send email: " + response.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/send-bulk")
    public ResponseEntity<?> sendBulkEmails(@RequestBody List<EmailRequest> emailRequests) {
        try {
            BulkSendReport report = new BulkSendReport();
            for (EmailRequest request : emailRequests) {
                try {
                    GmailEmailResponse response = gmailService.sendEmail(
                        request.getUserId(),
                        request.getToEmail(),
                        request.getToName(),
                        request.getSubject(),
                        request.getBody(),
                        request.getCompany(),
                        request.isAttachResume(),
                        request.getResumeFilename()
                    );
                    if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                        report.successCount++;
                    } else {
                        report.failureCount++;
                        report.errors.add(request.getToEmail() + " - " + response.getMessage());
                    }
                } catch (Exception ex) {
                    report.failureCount++;
                    report.errors.add(request.getToEmail() + " error: " + ex.getMessage());
                }
                // small delay to avoid rate limiting
               // small delay to avoid rate limiting
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send bulk emails: " + e.getMessage());
        }
    }

    public static class BulkSendReport {
        public int successCount = 0;
        public int failureCount = 0;
        public List<String> errors = new java.util.ArrayList<>();

        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public List<String> getErrors() { return errors; }
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

    @GetMapping("/serve-resume/{filename}")
    public ResponseEntity<?> serveResume(@PathVariable String filename) {
        try {
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest().body("Filename is required");
            }
            byte[] resumeBytes = employeeservice.getTempResumeBytes(filename);
            if (resumeBytes == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resumeBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error serving resume: " + e.getMessage());
        }
    }
}