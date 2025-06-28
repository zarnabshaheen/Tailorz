package com.example.tailorz.customerModels;

public class Feedback {
    private String customerName;
    private String feedbackText;
    private float rating;

    public Feedback() { }

    public Feedback(String customerName, String feedbackText, float rating) {
        this.customerName = customerName;
        this.feedbackText = feedbackText;
        this.rating = rating;
    }

    public String getCustomerName() { return customerName; }
    public String getFeedbackText() { return feedbackText; }
    public float getRating() { return rating; }
}
