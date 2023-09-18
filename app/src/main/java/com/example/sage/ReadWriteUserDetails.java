package com.example.sage;

public class ReadWriteUserDetails {
    public String emailaddress, DOB, gender,password;

    public ReadWriteUserDetails(String txtemailaddress,String txtDOB,String txtGender, String txtpassword){
        this.emailaddress = txtemailaddress;
        this.DOB = txtDOB;
        this.gender = txtGender;
        this.password = txtpassword;
    }
}
