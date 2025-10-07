package com.login.firstproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;

import java.util.ArrayList;
import java.util.List;

//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Employeeservice {

    public UserRecord createUser(String name,String email, String password,String uid) throws FirebaseAuthException {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setDisplayName(name)
                .setEmail(email)
                .setPassword(password).setUid(uid);

                UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                 //System.out.println("Successfully created new user: " + userRecord.getUid()); 
                 return userRecord;
    }

    public UserRecord getUserById(String uid) throws FirebaseAuthException {
        return FirebaseAuth.getInstance().getUser(uid);
    }

    public void deleteUser(String uid) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(uid);
    }
    public UserRecord updateUserById(String uid ,String name, String email ,String Password) throws FirebaseAuthException
    {
        try {
            UserRecord.UpdateRequest req=new UserRecord.UpdateRequest(uid).setEmail(email).setPassword(Password).setDisplayName(name);
            UserRecord user=FirebaseAuth.getInstance().updateUser(req);
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
       
        
    }
    public List<user> listAllUsers() throws FirebaseAuthException
    { try 
        { ListUsersPage page = FirebaseAuth.getInstance().listUsers(null); 
            List<user> l=new ArrayList<>();
            for (UserRecord u : page.getValues()) {
               user t=new user(u.getEmail(),u.getUid(),u.getDisplayName());
               l.add(t);
        } 
    return l;
    } 
            catch (FirebaseAuthException e) { e.printStackTrace();}
                return null;
}
} 
