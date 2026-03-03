package com.login.firstproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip the header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Parse CSV line
                String[] fields = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Handle quoted fields
                
                if (fields.length >= 3) {
                    String id = fields[0].trim().replaceAll("^\"|\"$", "");
                    String name = fields[1].trim().replaceAll("^\"|\"$", "");
                    String email = fields[2].trim().replaceAll("^\"|\"$", "");
                    
                    // Generate a default password if not provided
                    String password = fields.length > 3 ? 
                        fields[3].trim().replaceAll("^\"|\"$", "") : 
                        "TempPassword@123";
                    
                    try {
                        // Validate email format
                        if (isValidEmail(email) && !id.isEmpty() && !name.isEmpty()) {
                            createUser(name, email, password, id);
                            importedCount++;
                        }
                    } catch (FirebaseAuthException e) {
                        // Log error but continue with next user
                        System.err.println("Error importing user " + id + ": " + e.getMessage());
                    }
                }
            }
        }
        return importedCount;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public UserRecord createUser(String name, String email, String password, String uid, String resumePath) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setDisplayName(name)
                .setEmail(email)
                .setPassword(password)
                .setUid(uid);

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
        //System.out.println("Successfully created new user: " + userRecord.getUid()); 
        return userRecord;
    }

    public UserRecord createUser(String name, String email, String password, String uid) throws FirebaseAuthException {
        return createUser(name, email, password, uid, null);
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

    public UserRecord updateUserById(String uid, String name, String email, String password, String resumePath) throws FirebaseAuthException {
        try {
            UserRecord.UpdateRequest req = new UserRecord.UpdateRequest(uid);
            if (name != null) {
                req.setDisplayName(name);
            }
            if (email != null) {
                req.setEmail(email);
            }
            if (password != null) {
                req.setPassword(password);
            }
            UserRecord user = FirebaseAuth.getInstance().updateUser(req);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UserRecord updateUserById(String uid, String name, String email, String password) throws FirebaseAuthException {
        return updateUserById(uid, name, email, password, null);
    }

    public List<user> listAllUsers() throws FirebaseAuthException {
        try {
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null); 
            List<user> l = new ArrayList<>();
            for (UserRecord u : page.getValues()) {
               user t = new user(u.getEmail(), u.getUid(), u.getDisplayName());
               l.add(t);
            } 
            return l;
        } 
        catch (FirebaseAuthException e) { 
            e.printStackTrace();
        }
        return null;
    }
}
