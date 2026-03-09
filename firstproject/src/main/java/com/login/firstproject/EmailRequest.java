package com.login.firstproject;

public class EmailRequest {
    private String userId;
    private String toEmail;
    private String toName;
    private String subject;
    private String body;
    private String company;
    private boolean attachResume;
    private String resumeFilename;

    // Getters
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getToEmail() {
        return toEmail;
    }

    public String getToName() {
        return toName;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getCompany() {
        return company;
    }


    public boolean isAttachResume() {
        return attachResume;
    }

    public String getResumeFilename() {
        return resumeFilename;
    }

    // Setters

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setAttachResume(boolean attachResume) {
        this.attachResume = attachResume;
    }

    public void setResumeFilename(String resumeFilename) {
        this.resumeFilename = resumeFilename;
    }
}