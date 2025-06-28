package com.example.tailorz.customerAdapters;

public class CustomerFavouriteModel {
    private String Design_url;
    private String Design_name;
    private String Design_id;
    private String Tailor_username;
    private boolean isFavorite;

    public CustomerFavouriteModel() {
    }

    public CustomerFavouriteModel(String design_url, String design_name, String design_id, String tailor_username, boolean isFavorite) {
        Design_url = design_url;
        Design_name = design_name;
        Design_id = design_id;
        Tailor_username = tailor_username;
        this.isFavorite = isFavorite;
    }

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
        Design_id = design_id;
    }

    public String getTailor_username() {
        return Tailor_username;
    }

    public void setTailor_username(String tailor_username) {
        Tailor_username = tailor_username;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
