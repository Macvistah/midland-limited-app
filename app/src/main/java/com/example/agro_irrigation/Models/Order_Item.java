package com.example.agro_irrigation.Models;

public class Order_Item {
    private String order_id;
    private String order_no;
    private String order_status;
    private String order_amount;
    private String order_date;
    private String order_payment;
    private String order_address;
    private String order_charge;

    public Order_Item() {
    }

    public Order_Item(String order_id, String order_no, String order_amount) {
        this.order_id = order_id;
        this.order_no = order_no;
        this.order_amount = order_amount;
    }

    public Order_Item(String order_id, String order_no, String order_status, String order_amount, String order_date, String order_payment, String order_address, String order_charge) {
        this.order_id = order_id;
        this.order_no = order_no;
        this.order_status = order_status;
        this.order_amount = order_amount;
        this.order_date = order_date;
        this.order_payment = order_payment;
        this.order_address = order_address;
        this.order_charge = order_charge;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getOrder_amount() {
        return order_amount;
    }

    public void setOrder_amount(String order_amount) {
        this.order_amount = order_amount;
    }

    public String getOrder_date() {
        return order_date;
    }

    public void setOrder_date(String order_date) {
        this.order_date = order_date;
    }

    public String getOrder_payment() {
        return order_payment;
    }

    public void setOrder_payment(String order_payment) {
        this.order_payment = order_payment;
    }

    public String getOrder_address() {
        return order_address;
    }

    public void setOrder_address(String order_address) {
        this.order_address = order_address;
    }

    public String getOrder_charge() {
        return order_charge;
    }

    public void setOrder_charge(String order_charge) {
        this.order_charge = order_charge;
    }

    @Override
    public String toString() {
        return order_no;
    }
}

