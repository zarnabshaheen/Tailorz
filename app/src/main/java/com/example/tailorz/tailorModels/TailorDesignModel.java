package com.example.tailorz.tailorModels;

import android.util.Log;

public class TailorDesignModel {
    private String Design_url;
    private String Design_name;
    private String Design_id;
    private String tailor_whatsapp;

    private String Tailor_username;
    private String Tailor_phone;
    private String Tailor_address;
    private boolean isFavorite;
    private byte[] Design_image;

    // No-argument constructor required by Firebase
    public TailorDesignModel() {
    }

    public TailorDesignModel(String design_id, String design_name, byte[] design_image,
                             String tailor_username, String tailor_phone, String tailor_address, boolean isFavorite) {
        Design_id = design_id;
        Design_name = design_name;
        Design_image = design_image;
        Tailor_username = tailor_username;
        Tailor_phone = tailor_phone;
        Tailor_address = tailor_address;
        this.isFavorite = isFavorite;
    }

    // Getters and Setters for all fields
    public String getDesign_url() {
        return Design_url;
    }

    public void setDesign_url(String design_url) {
        Design_url = design_url;
    }

    public String getDesign_name() {
        return Design_name;
    }

    public void setDesign_name(String design_name) {
        Design_name = design_name;
    }

    public String getDesign_id() {
        return Design_id;
    }

    public void setDesign_id(String design_id) {
        if (design_id == null || design_id.isEmpty()) {
            Log.e("TailorDesignModel", "Attempting to set an invalid design ID!");
        }
        this.Design_id = design_id;
    }

    public String getTailor_username() {
        return Tailor_username;
    }

    public void setTailor_username(String tailor_username) {
        Tailor_username = tailor_username;
    }

    public String getTailor_phone() {
        return Tailor_phone;
    }

    public void setTailor_phone(String tailor_phone) {
        Tailor_phone = tailor_phone;
    }

    public String getTailor_address() {
        return Tailor_address;
    }

    public void setTailor_address(String tailor_address) {
        Tailor_address = tailor_address;
    }

    public void setTailor_whatsapp(String tailor_whatsapp) {
        this.tailor_whatsapp = tailor_whatsapp;
    }


    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public byte[] getDesign_image() {
        return Design_image;
    }

    public void setDesign_image(byte[] design_image) {
        this.Design_image = design_image;
    }
}
