package com.login.firstproject;

public class user {
    String id,email,password,name,resume;
    public user(){}
    public user(String e,String i,String password,String name){
    this.id=i;
    this.email=e;
    this.password=password;
    this.name=name;
}
public user(String e,String i,String n){
    this.id=i;
    this.email=e;
    this.name=n;
  
}
public String getname(){
    return name;}
    public void setname(String e)
    {
        name=e;
    }
public String getPassword(){
    return password;}
    public void setPassword(String e)
    {
        password=e;
    }
    public String getEmail(){
    return email;}
    public void setEmail(String e)
    {
        email=e;
    }
    public String getid(){
        return id;}
        public void setid(String i)
        {
            id=i;
        }
    public String getResume(){
        return resume;}
        public void setResume(String r)
        {
            resume=r;
        }

}

