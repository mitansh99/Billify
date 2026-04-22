package com.example.billifyfinal;

public class HelperClass {

    String username , phone , password, usertype;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public HelperClass(String username, String phone, String password, String usertype) {
        this.username = username;
        this.phone = phone;
        this.password = password;
        this.usertype = usertype;
    }

    public HelperClass() {
    }
}
