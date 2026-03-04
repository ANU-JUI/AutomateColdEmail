package com.login.firstproject;

public class user {
    String id, email, name, company, resume;

    public user() {}

    // constructor used when company is available (e.g. when listing users)
    public user(String email, String id, String name, String company) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.company = company;
    }

    // convenience constructor without company
    public user(String email, String id, String name) {
        this(email, id, name, null);
    }

    public String getname() {
        return name;
    }

    public void setname(String e) {
        name = e;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String e) {
        email = e;
    }

    public String getid() {
        return id;
    }

    public void setid(String i) {
        id = i;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String r) {
        resume = r;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

}

