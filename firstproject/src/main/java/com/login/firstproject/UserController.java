package com.login.firstproject;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
//import com.google.api.client.http.HttpHeaders;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import org.json.JSONObject;
//import org.apache.tomcat.util.http.parser.MediaType;
//import com.login.firstproject.Employeeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
 


@RestController
@CrossOrigin(origins = {
    "https://automatecoldemail.netlify.app",
    "http://localhost:3000"
})
@RequestMapping("/users")
public class UserController {

    @Value("${gmail.client.secret}")
    private String clientSecret;

     @Value("${gmail.client.id}")
    private String clientId;
    
    @Autowired
    private Employeeservice firebaseService;

private String exchangeCodeForToken(String code) throws Exception {

    String tokenEndpoint = "https://oauth2.googleapis.com/token";

    URL url = new URL(tokenEndpoint);

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    conn.setRequestMethod("POST");
    conn.setDoOutput(true);

    String params =
            "client_id=" + clientId +
            "&client_secret=" + clientSecret +
            "&code=" + code +
            "&grant_type=authorization_code" +
            "&redirect_uri=https://automatecoldemail-bakend.onrender.com/users/oauth/callback";

    OutputStream os = conn.getOutputStream();
    os.write(params.getBytes());
    os.flush();
    os.close();

    BufferedReader br =
            new BufferedReader(new InputStreamReader(conn.getInputStream()));

    String line;
    StringBuilder response = new StringBuilder();

    while ((line = br.readLine()) != null) {
        response.append(line);
    }

    br.close();

    JSONObject json = new JSONObject(response.toString());

    return json.getString("refresh_token");
}
    @PostMapping("/create")
    public ResponseEntity<?> createUser(
            @RequestParam(required = false) String id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String company)
    {
        try {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }

            // create user without handling any resume upload; resumes may be added later via update
            UserRecord userRecord = firebaseService.createUser(name, email, company, id);
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
    public List<user> getall(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            List<user> all = firebaseService.listAllUsers();
            if (all == null) return List.of();
            int start = page * size;
            if (start >= all.size()) return List.of();
            int end = Math.min(start + size, all.size());
            return all.subList(start, end);
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
    @GetMapping("/oauth/callback")
public ResponseEntity<?> oauthCallback(@RequestParam String code) throws Exception {

    String refreshToken = exchangeCodeForToken(code);

    String userId = UUID.randomUUID().toString();

    Firestore db = FirestoreClient.getFirestore();

    Map<String,Object> data = new HashMap<>();
    data.put("refreshToken", refreshToken);
    data.put("connected", true);
    data.put("createdAt", new Date());

    db.collection("gmailUsers")
      .document(userId)
      .set(data);

    String redirectUrl =
        "https://automatecoldemail.netlify.app/oauth-success?userId=" + userId;

    return ResponseEntity.status(302)
            .header("Location", redirectUrl)
            .build();
}
@PostMapping("/disconnect/{userId}")
public ResponseEntity<?> disconnect(@PathVariable String userId) {

    try {

        Firestore db = FirestoreClient.getFirestore();

        // 1. Fetch user document from Firestore
        DocumentReference userRef = db.collection("gmailUsers").document(userId);
        DocumentSnapshot snapshot = userRef.get().get();

        if (!snapshot.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String refreshToken = snapshot.getString("refreshToken");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
        }

        // 2. Call Google revoke endpoint
String revokeUrl = "https://oauth2.googleapis.com/revoke";

MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
body.add("token", refreshToken);
HttpHeaders headers =new HttpHeaders();
  headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
      
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> googleResponse =
                restTemplate.postForEntity(revokeUrl, entity, String.class);

        if (googleResponse.getStatusCode().is2xxSuccessful()) {

            // 3. Remove tokens from Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("refreshToken", FieldValue.delete());
            updates.put("accessToken", FieldValue.delete());

            userRef.update(updates);

            return ResponseEntity.ok("Successfully revoked and cleared");

        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Google failed to revoke token");
        }

    } catch (Exception e) {

        try {
            // Even if Google revoke fails → clear Firestore tokens
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference userRef = db.collection("gmailUsers").document(userId);

            Map<String, Object> updates = new HashMap<>();
            updates.put("refreshToken", FieldValue.delete());
            updates.put("accessToken", FieldValue.delete());

            userRef.update(updates).get();

        } catch (Exception ignored) {}

        return ResponseEntity.ok("Cleared locally after error: " + e.getMessage());
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
