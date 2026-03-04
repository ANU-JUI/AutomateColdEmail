package com.login.firstproject;

//import com.google.api.client.http.HttpHeaders;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

//import org.apache.tomcat.util.http.parser.MediaType;
//import com.login.firstproject.Employeeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = {
    "https://automatecoldemails.netlify.app/",
    "http://localhost:3000"
})
@RequestMapping("/users")
public class controller {

    @Autowired
    private Employeeservice firebaseService;


    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestParam(required = false) String id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String company,
            @RequestParam(required = false) MultipartFile resume)
    {
        try {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            String resumePath = null;
            if (resume != null && !resume.isEmpty()) {
                // if id is null we'll generate temporary id for file storage
                String useId = (id == null || id.isEmpty()) ? java.util.UUID.randomUUID().toString() : id;
                resumePath = firebaseService.saveResume(resume, useId);
            }
            UserRecord userRecord = firebaseService.createUser(name, email, company, id, resumePath);
            return ResponseEntity.ok(userRecord);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) { 
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"; 
        return email.matches(emailRegex);
    }

    @GetMapping("/{uid}")
    public user getUser(@PathVariable String uid) {
        try {
            UserRecord u = firebaseService.getUserById(uid);
            String company = null;
            if (u.getCustomClaims() != null && u.getCustomClaims().get("company") != null) {
                company = u.getCustomClaims().get("company").toString();
            }
            return new user(u.getEmail(), u.getUid(), u.getDisplayName(), company);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportEmployees() {
        try {
            List<user> allUsers = firebaseService.listAllUsers();
            StringBuilder csvData = new StringBuilder("ID,Name,Email,Company\n");

            for (user u : allUsers) {
                csvData.append(u.getid()).append(",")
                       .append(u.getname()).append(",")
                       .append(u.getEmail()).append(",")
                       .append(u.getCompany() == null ? "" : u.getCompany()).append("\n");
            }

            byte[] csvBytes = csvData.toString().getBytes(StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csvBytes);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/get")
    public List<user> getall() {
        try {
            return firebaseService.listAllUsers();
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/search/{query}")
    public List<user> searchUsers(@PathVariable String query) {
        try {
            // Get all users first
            List<user> allUsers = firebaseService.listAllUsers();

            // Filter users by name or email
            return allUsers.stream()
                    .filter(u -> u.getname().toLowerCase().contains(query.toLowerCase()) ||
                                 u.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                                 (u.getCompany() != null && u.getCompany().toLowerCase().contains(query.toLowerCase())) )
                    .toList();
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @GetMapping("/download-resume/{uid}")
    public ResponseEntity<?> downloadResume(@PathVariable String uid) {
        try {
            byte[] resumeBytes = firebaseService.getResumeBytes(uid);
            if (resumeBytes == null) {
                return ResponseEntity.notFound().build();
            }
            String resumeFilename = firebaseService.getResumeFilename(uid);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resumeFilename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resumeBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error downloading resume: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateuser(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) MultipartFile resume) {
        try {
            if (email != null && !isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }

            String resumePath = null;
            if (resume != null && !resume.isEmpty()) {
                resumePath = firebaseService.saveResume(resume, id);
            }

            UserRecord userRecord = firebaseService.updateUserById(id, name, email, company, resumePath);
            return ResponseEntity.ok(userRecord);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating user: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{uid}")
    public String deleteUser(@PathVariable String uid) {
        try {
            firebaseService.deleteUser(uid);
            return "User deleted successfully";
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return "Error deleting user";
        }
    }

    @PostMapping("/import-csv")
    public ResponseEntity<?> importCSV(@RequestParam MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "File is empty", "importedCount", 0)
                );
            }

            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Please upload a CSV file", "importedCount", 0)
                );
            }

            int importedCount = firebaseService.importUsersFromCSV(file);
            return ResponseEntity.ok(
                Map.of("message", "Successfully imported users", "importedCount", importedCount)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                Map.of("message", "Error importing CSV: " + e.getMessage(), "importedCount", 0)
            );
        }
    }
}
