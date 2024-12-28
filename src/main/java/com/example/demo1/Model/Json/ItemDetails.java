package com.example.demo1.Model.Json;

public class ItemDetails {

    private String level;
    private String price;

    public ItemDetails(String level, String price) {
        this.level = level;
        this.price = price;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ItemDetails{" +
                "level='" + level + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}