package com.here.hellomap;

public class UserModal {

    private int id;
    private String Name;
    private String BloodGroup;
    private String dateOfBirth;
    private double latitude;
    private double longitude;

    public UserModal(int id, String name, String bloodGroup, String dateOfBirth, double latitude, double longitude) {
        this.id = id;
        Name = name;
        BloodGroup = bloodGroup;
        this.dateOfBirth = dateOfBirth;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getBloodGroup() {
        return BloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        BloodGroup = bloodGroup;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
