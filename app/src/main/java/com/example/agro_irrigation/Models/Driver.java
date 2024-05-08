package com.example.agro_irrigation.Models;

import androidx.annotation.NonNull;

public class Driver {
    private String driverId;
    private String driverName;

    public Driver() {
    }

    public Driver(String driverId, String driverName) {
        this.driverId = driverId;
        this.driverName = driverName;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    @NonNull
    @Override
    public String toString() {
        return driverName;
    }
}
