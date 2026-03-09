package com.login.firstproject;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Properties;

import org.json.JSONObject;

@Service
public class GmailService {

    @Autowired
    private Employeeservice employeeservice;

    @Value("${gmail.client.id}")
    private String clientId;

    @Value("${gmail.client.secret}")
    private String clientSecret;

    public GmailEmailResponse sendEmail(
            String userId,
            String toEmail,
            String toName,
            String subject,
            String body,
            String company,
            boolean attachResume,
            String resumeFilename) {

        try {

            /* =============================
               1️⃣ Get refresh token
               ============================= */

            Firestore db = FirestoreClient.getFirestore();

            DocumentSnapshot doc =
                    db.collection("gmailUsers")
                            .document(userId)
                            .get()
                            .get();

            if (!doc.exists()) {
                throw new RuntimeException("User Gmail not connected");
            }

            String refreshToken = doc.getString("refreshToken");

            /* =============================
               2️⃣ Convert refreshToken → accessToken
               ============================= */

            String accessToken = getAccessToken(refreshToken);

            /* =============================
               3️⃣ Build Gmail Client
               ============================= */

            GoogleCredential credential =
                    new GoogleCredential().setAccessToken(accessToken);

            Gmail gmail =
                    new Gmail.Builder(
                                    GoogleNetHttpTransport.newTrustedTransport(),
                                    JacksonFactory.getDefaultInstance(),
                                    credential)
                            .setApplicationName("Cold Email Automation")
                            .build();

            /* =============================
               4️⃣ Replace placeholders
               ============================= */

            if (body != null) {
                body = body.replace("[HR_NAME]", toName == null ? "" : toName);

                if (company != null) {
                    body = body.replace("[COMPANY_NAME]", company);
                }

                body =
                        "<p>"
                                + body.replace("\n\n", "</p><p>").replace("\n", "<br>")
                                + "</p>";
            }

            if (subject != null) {

                subject = subject.replace("[HR_NAME]", toName == null ? "" : toName);

                if (company != null) {
                    subject = subject.replace("[COMPANY_NAME]", company);
                }
            }

            byte[] resumeBytes = null;

            if (attachResume && resumeFilename != null) {

                resumeBytes = employeeservice.getTempResumeBytes(resumeFilename);
            }

            /* =============================
               5️⃣ Create email
               ============================= */

            MimeMessage email =
                    createEmail(
                            toEmail,
                            "me",
                            subject,
                            body,
                            resumeBytes,
                            resumeFilename);

            Message message = createMessageWithEmail(email);

            /* =============================
               6️⃣ Send email
               ============================= */

            gmail.users().messages().send("me", message).execute();

            GmailEmailResponse response = new GmailEmailResponse();

            response.setStatusCode(200);
            response.setMessage("Email sent successfully");

            return response;

        } catch (Exception e) {

            GmailEmailResponse response = new GmailEmailResponse();

            response.setStatusCode(500);
            response.setMessage("Failed to send email: " + e.getMessage());

            return response;
        }
    }

    /* =============================
       Convert refresh token → access token
       ============================= */

    private String getAccessToken(String refreshToken) throws Exception {

        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        URL url = new URL(tokenEndpoint);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String params =
                "client_id="
                        + clientId
                        + "&client_secret="
                        + clientSecret
                        + "&refresh_token="
                        + refreshToken
                        + "&grant_type=refresh_token";

        OutputStream os = conn.getOutputStream();

        os.write(params.getBytes());
        os.close();

        BufferedReader br =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder response = new StringBuilder();
String line;

while ((line = br.readLine()) != null) {
    response.append(line);
}

JSONObject json = new JSONObject(response.toString());

        return json.getString("access_token");
    }

    /* =============================
       Create Email
       ============================= */

    private MimeMessage createEmail(
            String to,
            String from,
            String subject,
            String bodyText,
            byte[] resumeBytes,
            String resumeFilename)
            throws MessagingException {

        Properties props = new Properties();

        Session session = Session.getDefaultInstance(props);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));

        email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));

        email.setSubject(subject);

        MimeBodyPart textPart = new MimeBodyPart();

        textPart.setContent(bodyText, "text/html; charset=UTF-8");

        Multipart multipart = new MimeMultipart();

        multipart.addBodyPart(textPart);

        if (resumeBytes != null) {

            MimeBodyPart attachmentPart = new MimeBodyPart();

            attachmentPart.setFileName(resumeFilename);

            attachmentPart.setDataHandler(
                    new javax.activation.DataHandler(
                            new javax.mail.util.ByteArrayDataSource(
                                    resumeBytes,
                                    "application/octet-stream")));

            multipart.addBodyPart(attachmentPart);
        }

        email.setContent(multipart);

        return email;
    }

    private Message createMessageWithEmail(MimeMessage email)
            throws MessagingException, IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        email.writeTo(buffer);

        byte[] bytes = buffer.toByteArray();

        String encodedEmail =
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Message message = new Message();

        message.setRaw(encodedEmail);

        return message;
    }
}