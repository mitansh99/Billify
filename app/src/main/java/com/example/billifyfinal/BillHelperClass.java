package com.example.billifyfinal;

public class BillHelperClass {
String BillId ,ShopkeeperUsername, CustomerUsername ,Date  ;



    public String getBillId() {
        return BillId;
    }

    public void setBillId(String billId) {
        BillId = billId;
    }

    public String getShopkeeperUsername() {
        return ShopkeeperUsername;
    }

    public void setShopkeeperUsername(String shopkeeperUsername) {
        ShopkeeperUsername = shopkeeperUsername;
    }

    public String getCustomerUsername() {
        return CustomerUsername;
    }

    public void setCustomerUsername(String customerUsername) {
        CustomerUsername = customerUsername;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
    public BillHelperClass(String billId, String shopkeeperUsername, String customerUsername, String date) {
        BillId = billId;
        ShopkeeperUsername = shopkeeperUsername;
        CustomerUsername = customerUsername;
        Date = date;
    }
    public BillHelperClass() {
    }
}
