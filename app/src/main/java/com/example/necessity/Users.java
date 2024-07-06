package com.example.necessity;

public class Users {
    private String name, phone, password, ngoid, email;
    public Users()
    {

    }

    public Users(String name, String phone, String password, String ngoid, String email) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.ngoid = ngoid;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getNgoid() {
        return ngoid;
    }

    public void setNgoid(String ngoid) {
        this.ngoid = ngoid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String address) {
        this.email = email;
    }
}
