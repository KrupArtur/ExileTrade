package com.demo.poe.Model.POE2;

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