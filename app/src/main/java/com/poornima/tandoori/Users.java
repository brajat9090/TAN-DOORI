package com.poornima.tandoori;

public class Users {
    private String ID;
    private String username;
    private String test;
    private String result;
    private String phone;
    private String age;
    private String other;
    private double lat;
    private double lng;
    private boolean active;


    public Users(){

    }

    public Users(String username,String age, String test, String result,String other) {
        this.username = username;
        this.test = test;
        this.result = result;
        this.age = age;
        this.other = other;
        this.active = true;
    }
    public Users(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }
    public boolean getActive() { return active; }
    public double getLat(){
        return lat;
    }
    public double getLng(){
        return lng;
    }

    public String getID() {
        return ID;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }
    public String getAge() {
        return age;
    }

    public String getTest() {
        return test;
    }

    public String getResult() {
        return result;
    }

    public String getOther() {
        return other;
    }
}
