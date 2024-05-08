package com.example.agro_irrigation.Models;

public class Order_Item_Details {
    private String orderDetailId;
    private String order_prod_name;
    private String order_prod_qty;
    private String order_prod_price;
    private String order_specs;

    public Order_Item_Details() {
    }

    public Order_Item_Details(String orderDetailId, String order_prod_name, String order_prod_qty, String order_prod_price, String order_specs) {
        this.orderDetailId = orderDetailId;
        this.order_prod_name = order_prod_name;
        this.order_prod_qty = order_prod_qty;
        this.order_prod_price = order_prod_price;
        this.order_specs = order_specs;
    }

    public String getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(String orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public String getOrder_prod_name() {
        return order_prod_name;
    }

    public void setOrder_prod_name(String order_prod_name) {
        this.order_prod_name = order_prod_name;
    }

    public String getOrder_prod_qty() {
        return order_prod_qty;
    }

    public void setOrder_prod_qty(String order_prod_qty) {
        this.order_prod_qty = order_prod_qty;
    }

    public String getOrder_prod_price() {
        return order_prod_price;
    }

    public void setOrder_prod_price(String order_prod_price) {
        this.order_prod_price = order_prod_price;
    }

    public String getOrder_specs() {
        return order_specs;
    }

    public void setOrder_specs(String order_specs) {
        this.order_specs = order_specs;
    }
}
