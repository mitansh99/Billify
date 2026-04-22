package com.example.billifyfinal;

public class ItemHelperclass {

    String itemName,itemDec;
    String itemId;
    String itemPrice;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDec() {
        return itemDec;
    }

    public void setItemDec(String itemDec) {
        this.itemDec = itemDec;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public ItemHelperclass(String itemId, String itemName, String itemPrice, String itemDec) {
        this.itemName = itemName;
        this.itemDec = itemDec;
        this.itemId = itemId;
        this.itemPrice = itemPrice;
    }

    public ItemHelperclass() {
    }
}
