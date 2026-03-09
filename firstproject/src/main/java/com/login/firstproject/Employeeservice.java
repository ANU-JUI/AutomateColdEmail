package com.login.firstproject;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Employeeservice {

    private static final String UPLOAD_DIR = "uploads/resumes/";
   
    public Employeeservice() {
        // Create the upload directory if it doesn't exist
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    public String saveResume(MultipartFile file, String userId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Create filename with userId to ensure uniqueness
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = userId + "_resume" + fileExtension;
        
        Path filepath = Paths.get(UPLOAD_DIR + filename);
        Files.write(filepath, file.getBytes());
        
        return filename;
    }

    public String saveTempResume(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = System.currentTimeMillis() + "_resume" + fileExtension;
        Path filepath = Paths.get(UPLOAD_DIR + filename);
        Files.write(filepath, file.getBytes());
        return filename;
    }

    public byte[] getTempResumeBytes(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) return null;
        Path filepath = Paths.get(UPLOAD_DIR + filename);
        if (!Files.exists(filepath)) return null;
        return Files.readAllBytes(filepath);
    }

    public byte[] getResumeBytes(String userId) throws IOException {
        // Search for the resume file for this user
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            return null;
        }

        File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(userId + "_resume"));
        if (files != null && files.length > 0) {
            return Files.readAllBytes(files[0].toPath());
        }
        return null;
    }

    public String getResumeFilename(String userId) {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            return null;
        }

        File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(userId + "_resume"));
        if (files != null && files.length > 0) {
            return files[0].getName();
        }
        return "resume.pdf";
    }

    public int importUsersFromCSV(MultipartFile file) throws IOException, FirebaseAuthException {
        int importedCount = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return 0;

            // determine column order from header (case-insensitive)
            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            int idxName = -1, idxEmail = -1, idxCompany = -1;
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().replaceAll("^\"|\"$", "").toLowerCase();
                if (h.equals("name") || h.equals("full name") || h.equals("fullname")) idxName = i;
                else if (h.equals("email") || h.equals("email address") || h.equals("e-mail")) idxEmail = i;
                else if (h.equals("company") || h.equals("employer") || h.equals("organisation") || h.equals("organization")) idxCompany = i;
            }

            // Fallback to positional mapping if headers are not recognized
            if (idxName == -1 || idxEmail == -1) {
                idxName = 0;
                idxEmail = 1;
                idxCompany = Math.max(2, idxCompany);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                String name = idxName < fields.length ? fields[idxName].trim().replaceAll("^\"|\"$", "") : "";
                String email = idxEmail < fields.length ? fields[idxEmail].trim().replaceAll("^\"|\"$", "") : "";
                String company = idxCompany < fields.length ? fields[idxCompany].trim().replaceAll("^\"|\"$", "") : "";

                try {
                    if (isValidEmail(email) && !name.isEmpty()) {
                        createUser(name, email, company, null);
                        importedCount++;
                    }
                } catch (FirebaseAuthException e) {
                    System.err.println("Error importing user " + email + ": " + e.getMessage());
                }
            }
        }
        return importedCount;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    // create a new Firebase user; resumes are handled separately through update endpoint
    // if no uid provided, we generate a sequential numeric identifier (1,2,3,...)
    public UserRecord createUser(String name, String email, String company, String uid) throws FirebaseAuthException {
        if (uid == null || uid.isEmpty()) {
            uid = generateNextNumericUid();
        }

        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setDisplayName(name)
                .setEmail(email)
                .setUid(uid);

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        if (company != null) {
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), java.util.Map.of("company", company));
        }
        return userRecord;
    }

    public UserRecord getUserById(String uid) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().getUser(uid);
    }

    public void deleteUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
        // Also delete the resume file if it exists
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (uploadDir.exists()) {
                File[] files = uploadDir.listFiles((dir, name) -> name.startsWith(uid + "_resume"));
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserRecord updateUserById(String uid, String name, String email, String company, String resumePath) throws FirebaseAuthException {
        try {
            UserRecord.UpdateRequest req = new UserRecord.UpdateRequest(uid);
            if (name != null) {
                req.setDisplayName(name);
            }
            if (email != null) {
                req.setEmail(email);
            }
            UserRecord user = FirebaseAuth.getInstance().updateUser(req);
            // update company custom claim if provided
            if (company != null) {
                FirebaseAuth.getInstance().setCustomUserClaims(uid, java.util.Map.of("company", company));
            }
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void saveToken(String userId, String refreshToken) throws Exception {

    Firestore db = FirestoreClient.getFirestore();

    Map<String, Object> data = new HashMap<>();

    data.put("refreshToken", refreshToken);
    data.put("connected", true);
    data.put("createdAt", new Date());

    db.collection("gmailUsers")
      .document(userId)
      .set(data);
}

    public UserRecord updateUserById(String uid, String name, String email, String company) throws FirebaseAuthException {
        return updateUserById(uid, name, email, company, null);
    }

    public List<user> listAllUsers() throws FirebaseAuthException {
        try {
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
            List<user> l = new ArrayList<>();
            for (UserRecord u : page.getValues()) {
               String company = null;
               if (u.getCustomClaims() != null && u.getCustomClaims().get("company") != null) {
                   company = u.getCustomClaims().get("company").toString();
               }
               user t = new user(u.getEmail(), u.getUid(), u.getDisplayName(), company);
               l.add(t);
            }
            return l;
        }
        catch (FirebaseAuthException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String generateNextNumericUid() throws FirebaseAuthException {
        long max = 0;
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        while (page != null) {
            for (UserRecord u : page.getValues()) {
                String id = u.getUid();
                if (id != null && id.matches("\\d+")) {
                    try {
                        long v = Long.parseLong(id);
                        if (v > max) max = v;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            page = page.getNextPage();
        }
        return String.valueOf(max + 1);
    }
}
