package com.example.agro_irrigation.Models;

public class Purchases {
    private String id;
    private String purchase_no;
    private String purchase_date;
    private String product_id;
    private String product_name;
    private String status;
    private String supplier_id;
    private String description;
    private String supplier_name;
    private String payment_code;
    private String supplier_company;
    private int original_qty;
    private int available_qty;
    private int price;

    private int final_price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPurchase_no() {
        return purchase_no;
    }

    public void setPurchase_no(String purchase_no) {
        this.purchase_no = purchase_no;
    }

    public String getPurchase_date() {
        return purchase_date;
    }

    public void setPurchase_date(String purchase_date) {
        this.purchase_date = purchase_date;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id) {
        this.supplier_id = supplier_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSupplier_name() {
        return supplier_name;
    }

    public void setSupplier_name(String supplier_name) {
        this.supplier_name = supplier_name;
    }

    public String getPayment_code() {
        return payment_code;
    }

    public void setPayment_code(String payment_code) {
        this.payment_code = payment_code;
    }

    public String getSupplier_company() {
        return supplier_company;
    }

    public void setSupplier_company(String supplier_company) {
        this.supplier_company = supplier_company;
    }

    public int getOriginal_qty() {
        return original_qty;
    }

    public void setOriginal_qty(int original_qty) {
        this.original_qty = original_qty;
    }

    public int getAvailable_qty() {
        return available_qty;
    }

    public void setAvailable_qty(int available_qty) {
        this.available_qty = available_qty;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getFinal_price() {
        return final_price;
    }

    public void setFinal_price(int final_price) {
        this.final_price = final_price;
    }

    public Purchases() {
    }


}

