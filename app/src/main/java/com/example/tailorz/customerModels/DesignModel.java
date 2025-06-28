package com.example.tailorz.customerModels;

public class DesignModel {
    private String designId;
    private String designName;
    private String designUrl;

    public DesignModel() {
        // Default constructor
    }

    public DesignModel(String designId, String designName, String designUrl) {
        this.designId = designId;
        this.designName = designName;
        this.designUrl = designUrl;
    }

    public String getDesignId() {
        return designId;
    }

    public void setDesignId(String designId) {
        this.designId = designId;
    }

    public String getDesignName() {
        return designName;
    }

    public void setDesignName(String designName) {
        this.designName = designName;
    }

    public String getDesignUrl() {
        return designUrl;
    }

    public void setDesignUrl(String designUrl) {
        this.designUrl = designUrl;
    }
}
