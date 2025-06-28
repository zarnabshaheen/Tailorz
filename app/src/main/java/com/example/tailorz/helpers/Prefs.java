package com.example.tailorz.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Prefs {
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    final static String SUBS_DATA = "userData";

    public Prefs(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(SUBS_DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    public void setUserName(String value) {
        Log.d("Prefs", "Saving UserName: " + value);
        editor.putString("userName", value);
        editor.apply();
    }

    public void setLoginStatus(boolean value) {
        Log.d("Prefs", "Saving Login Status: " + value);
        editor.putBoolean("isLogin", value);
        editor.apply();
    }

    public void setUserEmail(String value) {
        Log.d("Prefs", "Saving UserEmail: " + value);
        editor.putString("email", value);
        editor.apply();
    }

    public void setUserCategory(String value) {
        Log.d("Prefs", "Saving UserCategory: " + value);
        editor.putString("category", value);
        editor.apply();
    }

    public void setUserAddress(String value) {
        Log.d("Prefs", "Saving UserAddress: " + value);
        editor.putString("address", value);
        editor.apply();
    }

    public void setUserTelephone(String value) {
        Log.d("Prefs", "Saving UserTelephone: " + value);
        editor.putString("telephone", value);
        editor.apply();
    }

    public void setUserWhatsapp(String value) {
        Log.d("Prefs", "Saving UserWhatsapp: " + value);
        editor.putString("whatsapp", value);
        editor.apply();
    }

    public void setUserProfileImage(String value) {
        Log.d("Prefs", "Saving UserProfileImage: " + value);
        editor.putString("profile_url", value);
        editor.apply();
    }

    public void setTailorCategory(String value) {
        Log.d("Prefs", "Saving TailorCategory: " + value);
        editor.putString("tailor_category", value);
        editor.apply();
    }

    public void setUserArms(String value) {
        Log.d("Prefs", "Saving UserArms: " + value);
        editor.putString("user_arms", value);
        editor.apply();
    }

    public void setUserLegs(String value) {
        Log.d("Prefs", "Saving UserLegs: " + value);
        editor.putString("user_legs", value);
        editor.apply();
    }

    public void setUserWaist(String value) {
        Log.d("Prefs", "Saving UserWaist: " + value);
        editor.putString("user_waist", value);
        editor.apply();
    }

    public void setUserFull(String value) {
        Log.d("Prefs", "Saving UserFull: " + value);
        editor.putString("user_full", value);
        editor.apply();
    }

    public void setUserChest(String value) {
        Log.d("Prefs", "Saving UserChest: " + value);
        editor.putString("user_chest", value);
        editor.apply();
    }

    public void setUserGender(String value) {
        Log.d("Prefs", "Saving UserGender: " + value);
        editor.putString("user_gender", value);
        editor.apply();
    }

    public void setUserID(String value) {
        Log.d("Prefs", "Saving UserID: " + value);
        editor.putString("userID", value);
        editor.apply();
    }

    // ✅ LOG DATA WHEN RETRIEVING
    public String getUserName() {
        String value = sharedPreferences.getString("userName", null);
        Log.d("Prefs", "Retrieved UserName: " + value);
        return value;
    }

    public String getUserEmail() {
        String value = sharedPreferences.getString("email", null);
        Log.d("Prefs", "Retrieved UserEmail: " + value);
        return value;
    }

    public String getUserCategory() {
        String value = sharedPreferences.getString("category", null);
        Log.d("Prefs", "Retrieved UserCategory: " + value);
        return value;
    }

    public String getUserAddress() {
        String value = sharedPreferences.getString("address", null);
        Log.d("Prefs", "Retrieved UserAddress: " + value);
        return value;
    }

    public String getUserTelephone() {
        String value = sharedPreferences.getString("telephone", null);
        Log.d("Prefs", "Retrieved UserTelephone: " + value);
        return value;
    }

    public String getUserWhatsapp() {
        String value = sharedPreferences.getString("whatsapp", null);
        Log.d("Prefs", "Retrieved UserWhatsapp: " + value);
        return value;
    }

    public String getUserProfileImage() {
        String value = sharedPreferences.getString("profile_url", null);
        Log.d("Prefs", "Retrieved UserProfileImage: " + value);
        return value;
    }

    public String getTailorCategory() {
        String value = sharedPreferences.getString("tailor_category", null);
        Log.d("Prefs", "Retrieved TailorCategory: " + value);
        return value;
    }

    public String getUserGender() {
        String value = sharedPreferences.getString("user_gender", null);
        Log.d("Prefs", "Retrieved UserGender: " + value);
        return value;
    }

    public String getUserArms() {
        String value = sharedPreferences.getString("user_arms", null);
        Log.d("Prefs", "Retrieved UserArms: " + value);
        return value;
    }

    public String getUserLegs() {
        String value = sharedPreferences.getString("user_legs", null);
        Log.d("Prefs", "Retrieved UserLegs: " + value);
        return value;
    }

    public String getUserWaist() {
        String value = sharedPreferences.getString("user_waist", null);
        Log.d("Prefs", "Retrieved UserWaist: " + value);
        return value;
    }

    public String getUserFull() {
        String value = sharedPreferences.getString("user_full", null);
        Log.d("Prefs", "Retrieved UserFull: " + value);
        return value;
    }

    public String getUserChest() {
        String value = sharedPreferences.getString("user_chest", null);
        Log.d("Prefs", "Retrieved UserChest: " + value);
        return value;
    }

    public Boolean getLoginStatus() {
        Boolean value = sharedPreferences.getBoolean("isLogin", false);
        Log.d("Prefs", "Retrieved LoginStatus: " + value);
        return value;
    }

    public String getUserID() {
        String value = sharedPreferences.getString("userID", null);
        Log.d("Prefs", "Retrieved UserID: " + value);
        return value;
    }
    public void setMeasurementSaved(boolean status) {
        sharedPreferences.edit().putBoolean("measurement_saved", status).apply();
    }

    public boolean isMeasurementSaved() {
        return sharedPreferences.getBoolean("measurement_saved", false);
    }

    public void setRemovedOrders(List<String> removedOrders) {
        String removedOrdersString = String.join(",", removedOrders);
        editor.putString("removed_orders", removedOrdersString);
        editor.apply();
    }

    public List<String> getRemovedOrders() {
        String removedOrdersString = sharedPreferences.getString("removed_orders", "");
        if (removedOrdersString.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(removedOrdersString.split(",")));
    }

}
