package com.login.firstproject;

import lombok.Data;

@Data
public class EmailRequest {
    private String toEmail;
    private String toName;
    private String subject;
    private String body;
    private boolean includeResumeLink;
    private boolean attachResume;
}