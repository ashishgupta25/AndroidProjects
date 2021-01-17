package com.jobpe.javabean;

import android.location.Location;

import java.util.Date;

public class Employee {
    String objectId;
    byte[] bytePhoto;
    String jobTitle;
    String firstName;
    String lastName;
    String gender;
    Date birthDate;
    String profession;
    String experience = "0";
    String salary = "0";
    String about;
    String phoneNumber;
    String email;
    String city;
    String pincode;
    Location location;//during registration
    Date updateOn;
    boolean active = true;

    String distance;//during search, default distance

    public String getObjectId() {
        return objectId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        String capitalizeFirstLetter = firstName;
        if (firstName != null && firstName.length() > 1)
            capitalizeFirstLetter = firstName.substring(0, 1).toUpperCase() + firstName.substring(1).toLowerCase();
        this.firstName = capitalizeFirstLetter;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        String capitalizeFirstLetter = lastName;
        if (lastName != null && lastName.length() > 1)
            capitalizeFirstLetter = lastName.substring(0, 1).toUpperCase() + lastName.substring(1).toLowerCase();
        this.lastName = capitalizeFirstLetter;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getUpdateOn() {
        return updateOn;
    }

    public void setUpdateOn(Date updateOn) {
        this.updateOn = updateOn;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
