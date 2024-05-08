package com.example.agro_irrigation.Models;

public class Cart_Item {
    private String cart_id;
    private String cart_prod_id;
    private String cart_prod_name;
    private String cart_prod_qty;
    private String cart_prod_discount="0";
    private String cart_qty;
    private String cart_prod_price;
    private String cart_prod_img;
    private String cart_prod_Category;
    private String cart_specs;
    private String cart_prod_desc;

    public Cart_Item(String cart_id, String cart_prod_id, String cart_prod_name, String cart_prod_qty, String cart_prod_discount, String cart_qty, String cart_prod_price, String cart_prod_img, String cart_prod_Category, String cart_specs, String cart_prod_desc) {
        this.cart_id = cart_id;
        this.cart_prod_id = cart_prod_id;
        this.cart_prod_name = cart_prod_name;
        this.cart_prod_qty = cart_prod_qty;
        this.cart_prod_discount = cart_prod_discount;
        this.cart_qty = cart_qty;
        this.cart_prod_price = cart_prod_price;
        this.cart_prod_img = cart_prod_img;
        this.cart_prod_Category = cart_prod_Category;
        this.cart_specs = cart_specs;
        this.cart_prod_desc = cart_prod_desc;
    }

    public Cart_Item() {
    }
    public String getCart_id() {
        return cart_id;
    }

    public void setCart_id(String cart_id) {
        this.cart_id = cart_id;
    }

    public String getCart_prod_id() {
        return cart_prod_id;
    }

    public void setCart_prod_id(String cart_prod_id) {
        this.cart_prod_id = cart_prod_id;
    }

    public String getCart_prod_name() {
        return cart_prod_name;
    }

    public void setCart_prod_qty(String cart_prod_qty) {
        this.cart_prod_qty = cart_prod_qty;
    }
    public String getCart_prod_qty() {
        return cart_prod_qty;
    }

    public void setCart_prod_name(String cart_prod_name) {
        this.cart_prod_name = cart_prod_name;
    }

    public String getCart_qty() {
        return cart_qty;
    }

    public void setCart_qty(String cart_qty) {
        this.cart_qty = cart_qty;
    }

    public String getCart_prod_price() {
        return cart_prod_price;
    }

    public void setCart_prod_price(String cart_prod_price) {
        this.cart_prod_price = cart_prod_price;
    }
    public String getCart_prod_img() {
        return cart_prod_img;
    }

    public void setCart_prod_img(String cart_prod_img) {
        this.cart_prod_img = cart_prod_img;
    }

    public String getCart_prod_Category() {
        return cart_prod_Category;
    }


    public void setCart_prod_Category(String cart_prod_Category) {
        this.cart_prod_Category = cart_prod_Category;
    }
    public void setCart_specs(String cart_specs) {
        this.cart_specs = cart_specs;
    }
    public String getCart_specs() {
        return cart_specs;
    }
    public void setCart_prod_desc(String cart_prod_desc) {
        this.cart_prod_desc = cart_prod_desc;
    }
    public String getCart_prod_desc() {
        return cart_prod_desc;
    }

    public String getCart_prod_discount() {
        return cart_prod_discount;
    }

    public void setCart_prod_discount(String cart_prod_discount) {
        this.cart_prod_discount = cart_prod_discount;
    }
}
