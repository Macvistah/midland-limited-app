package com.example.agro_irrigation.Models;

public class Products {

    String productId;
    String productName;
    String productCat;
    String productQty;
    String productPrice;
    String productDiscount="0";
    String productDesc;
    String imageUrl;

    public String getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(String productDiscount) {
        this.productDiscount = productDiscount;
    }

    public Products() {
    }

    public Products(String productId, String productName, String productCat, String productQty, String productPrice, String productDiscount, String productDesc, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productCat = productCat;
        this.productQty = productQty;
        this.productPrice = productPrice;
        this.productDiscount = productDiscount;
        this.productDesc = productDesc;
        this.imageUrl = imageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
    public String getProductCat() {
        return productCat;
    }

    public void setProductCat(String productCat) {
        this.productCat = productCat;
    }

    public String getProductQty() {
        return productQty;
    }

    public void setProductQty(String productQty) {
        this.productQty = productQty;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
