package com.jobpe.javabean;

import android.location.Location;

public class Business {
    String phoneNumber;
    byte[] bytePhoto;
    String businessName;
    String industry;
    String about;
    String email;
    String city;
    String pincode;
    Location location;//during registration
    String address;
    String distance;//during search, default distance

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getBytePhoto() {
        return bytePhoto;
    }

    public void setBytePhoto(byte[] bytePhoto) {
        this.bytePhoto = bytePhoto;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        String capitalizeFirstLetter = businessName;
        if (businessName != null && businessName.length() > 1)
            capitalizeFirstLetter = businessName.substring(0, 1).toUpperCase() + businessName.substring(1).toLowerCase();
        this.businessName = capitalizeFirstLetter;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
