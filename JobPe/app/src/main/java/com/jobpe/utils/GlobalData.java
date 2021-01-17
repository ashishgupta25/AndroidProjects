package com.jobpe.utils;

import android.location.Location;

public class GlobalData {
    //static Location saveLocation;
    static boolean profilePhotoSet = false;
    static boolean locationSet = false;

    /*
    public static Location getSaveLocation() {
        return saveLocation;
    }

    public static void setSaveLocation(Location saveLocation) {
        GlobalData.saveLocation = saveLocation;
    }*/

    public static boolean isProfilePhotoSet() {
        return profilePhotoSet;
    }

    public static void setProfilePhotoSet(boolean profilePhotoSet) {
        GlobalData.profilePhotoSet = profilePhotoSet;
    }

    public static boolean isLocationSet() {
        return locationSet;
    }

    public static void setLocationSet(boolean locationSet) {
        GlobalData.locationSet = locationSet;
    }
}
