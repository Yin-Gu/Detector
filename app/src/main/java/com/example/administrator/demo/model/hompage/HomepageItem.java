package com.example.administrator.demo.model.hompage;

public class HomepageItem {
    private String itemName;
    private int itemImg;

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemImg(int itemImg) {
        this.itemImg = itemImg;
    }

    public String getItemName() {

        return itemName;
    }

    public int getItemImg() {
        return itemImg;
    }

    public HomepageItem(String itemName, int itemImg) {

        this.itemName = itemName;
        this.itemImg = itemImg;
    }
}
