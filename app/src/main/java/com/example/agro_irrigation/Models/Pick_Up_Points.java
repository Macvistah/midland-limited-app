package com.example.agro_irrigation.Models;

import androidx.annotation.NonNull;

public class Pick_Up_Points {
    String id;
    String name;
    Double charge;

    public Pick_Up_Points() {
    }

    public Pick_Up_Points(String id, String name, Double charge) {
        this.id = id;
        this.name = name;
        this.charge = charge;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCharge() {
        return charge;
    }

    public void setCharge(Double charge) {
        this.charge = charge;
    }
    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
