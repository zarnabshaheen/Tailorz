package com.example.tailorz.customerModels;

public class OrderModel {

    String tailorName, CustomerName, TailorWhatsapp, TailorPhone, CustomerWhatsapp, CustomerPhone,
    CustomerAddress, TailorAddress, DesignID, DesignName, CustomerShoulderMeasurement,CustomerChestMeasurement,
            CustomerWaistMeasurement, CustomerCollarMeasurement, CustomerGender, DesignUrl, TotalPrice, OrderDate,
    CompletionExpectedBy, PickupOrDelivery, OrderID;

    public OrderModel() {
    }

    public OrderModel(String tailorName,String orderID, String customerName, String tailorWhatsapp, String tailorPhone, String customerWhatsapp, String customerPhone, String customerAddress, String tailorAddress, String designID, String designName, String customerShoulderMeasurement, String customerChestMeasurement, String customerWaistMeasurement, String customerCollarMeasurement, String customerGender, String designUrl, String totalPrice, String orderDate, String completionExpectedBy, String pickupOrDelivery) {
        this.tailorName = tailorName;
        CustomerName = customerName;
        TailorWhatsapp = tailorWhatsapp;
        TailorPhone = tailorPhone;
        CustomerWhatsapp = customerWhatsapp;
        CustomerPhone = customerPhone;
        CustomerAddress = customerAddress;
        TailorAddress = tailorAddress;
        DesignID = designID;
        DesignName = designName;
        CustomerShoulderMeasurement = customerShoulderMeasurement;
        CustomerChestMeasurement = customerChestMeasurement;
        CustomerWaistMeasurement = customerWaistMeasurement;
        CustomerCollarMeasurement = customerCollarMeasurement;
        CustomerGender = customerGender;
        DesignUrl = designUrl;
        TotalPrice = totalPrice;
        OrderDate = orderDate;
        CompletionExpectedBy = completionExpectedBy;
        PickupOrDelivery = pickupOrDelivery;
        OrderID = orderID;
    }

    public String getOrderID() {
        return OrderID;
    }

    public void setOrderID(String orderID) {
        OrderID = orderID;
    }

    public String getTailorName() {
        return tailorName;
    }

    public void setTailorName(String tailorName) {
        this.tailorName = tailorName;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getTailorWhatsapp() {
        return TailorWhatsapp;
    }

    public void setTailorWhatsapp(String tailorWhatsapp) {
        TailorWhatsapp = tailorWhatsapp;
    }

    public String getTailorPhone() {
        return TailorPhone;
    }

    public void setTailorPhone(String tailorPhone) {
        TailorPhone = tailorPhone;
    }

    public String getCustomerWhatsapp() {
        return CustomerWhatsapp;
    }

    public void setCustomerWhatsapp(String customerWhatsapp) {
        CustomerWhatsapp = customerWhatsapp;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public String getCustomerAddress() {
        return CustomerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        CustomerAddress = customerAddress;
    }

    public String getTailorAddress() {
        return TailorAddress;
    }

    public void setTailorAddress(String tailorAddress) {
        TailorAddress = tailorAddress;
    }

    public String getDesignID() {
        return DesignID;
    }

    public void setDesignID(String designID) {
        DesignID = designID;
    }

    public String getDesignName() {
        return DesignName;
    }

    public void setDesignName(String designName) {
        DesignName = designName;
    }

    public String getCustomerShoulderMeasurement() {
        return CustomerShoulderMeasurement;
    }

    public void setCustomerShoulderMeasurement(String customerShoulderMeasurement) {
        CustomerShoulderMeasurement = customerShoulderMeasurement;
    }

    public String getCustomerChestMeasurement() {
        return CustomerChestMeasurement;
    }

    public void setCustomerChestMeasurement(String customerChestMeasurement) {
        CustomerChestMeasurement = customerChestMeasurement;
    }

    public String getCustomerWaistMeasurement() {
        return CustomerWaistMeasurement;
    }

    public void setCustomerWaistMeasurement(String customerWaistMeasurement) {
        CustomerWaistMeasurement = customerWaistMeasurement;
    }

    public String getCustomerCollarMeasurement() {
        return CustomerCollarMeasurement;
    }

    public void setCustomerCollarMeasurement(String customerCollarMeasurement) {
        CustomerCollarMeasurement = customerCollarMeasurement;
    }

    public String getCustomerGender() {
        return CustomerGender;
    }

    public void setCustomerGender(String customerGender) {
        CustomerGender = customerGender;
    }

    public String getDesignUrl() {
        return DesignUrl;
    }

    public void setDesignUrl(String designUrl) {
        DesignUrl = designUrl;
    }

    public String getTotalPrice() {
        return TotalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        TotalPrice = totalPrice;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }

    public String getCompletionExpectedBy() {
        return CompletionExpectedBy;
    }

    public void setCompletionExpectedBy(String completionExpectedBy) {
        CompletionExpectedBy = completionExpectedBy;
    }

    public String getPickupOrDelivery() {
        return PickupOrDelivery;
    }

    public void setPickupOrDelivery(String pickupOrDelivery) {
        PickupOrDelivery = pickupOrDelivery;
    }
}
