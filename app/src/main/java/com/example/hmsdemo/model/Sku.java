package com.example.hmsdemo.model;

public class Sku {
    private String productName;
    private String productId;
    private int priceType;
    private double price;
    private Object o;

    public Sku(String productName, String productId, int priceType, double price){
        this.productName = productName;
        this.productId = productId;
        this.priceType = priceType;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductId() {
        return productId;
    }

    public int getPriceType() {
        return priceType;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return productName;
    }
}
