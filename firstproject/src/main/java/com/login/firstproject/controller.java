package com.login.firstproject;

//import com.google.api.client.http.HttpHeaders;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

//import org.apache.tomcat.util.http.parser.MediaType;
//import com.login.firstproject.Employeeservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = {
    "https://automatecoldemail-frontend.onrender.com",
    "http://localhost:3000"
})
@RequestMapping("/users")
public class controller {

    @Autowired
    private Employeeservice firebaseService;


    @PostMapping("/create")
    public UserRecord createUser(@RequestBody user createUserRequest) 
    { try { String email = createUserRequest.getEmail();
         String password = createUserRequest.getPassword(); 
         String id = createUserRequest.getid(); 
         String name=createUserRequest.getname();
         if (!isValidEmail(email)) { throw new IllegalArgumentException("Invalid email format: " + email); }
          return firebaseService.createUser(name,email, password, id); } 
          catch (FirebaseAuthException e) { e.printStackTrace(); return null; } }
           private boolean isValidEmail(String email) { String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"; return email.matches(emailRegex);
    }
    @GetMapping("/{uid}")
    public UserRecord getUser(@PathVariable String uid) {
        try {
            return firebaseService.getUserById(uid);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/export")
public ResponseEntity<byte[]> exportEmployees() {
    try {
        List<user> allUsers = firebaseService.listAllUsers();
        StringBuilder csvData = new StringBuilder("ID,Name,Email\n");

        for (user u : allUsers) {
            csvData.append(u.getid()).append(",")
                   .append(u.getname()).append(",")
                   .append(u.getEmail()).append("\n");
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
                             u.getEmail().toLowerCase().contains(query.toLowerCase()))
                .toList();
    } catch (FirebaseAuthException e) {
        e.printStackTrace();
        return List.of();
    }
}

    @PutMapping("/update/{id}")
    public UserRecord updateuser(@PathVariable String id,@RequestBody user u) {
        try {
            String email = u.getEmail();
         String password = u.getPassword();
         String name = u.getname();
         if (!isValidEmail(email)) { throw new IllegalArgumentException("Invalid email format: " + email); }
            return firebaseService.updateUserById(id,name, email, password);
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return null;
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
}
