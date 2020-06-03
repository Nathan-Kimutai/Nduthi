package com.deveint.user.Models;

/**
 * Created by CSS on 25-11-2017.
 */

public class TripStatus {

    String deliveryAddress;
    String comments;
    String status;
    String d_lat;
    String d_long;

    public String getdeliveryAddress() {
        return deliveryAddress;
    }

    public void setdeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getcomments() {
        return comments;
    }

    public void setcomments(String comments) {
        this.comments = comments;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public void setD_lat(String d_lat) {
        this.d_lat = d_lat;
    }

    public String getD_lat() {
        return d_lat;
    }

    public void setD_long(String d_long) {
        this.d_long = d_long;
    }

    public String getD_long() {
        return d_long;
    }

    public String toString() {
        return "TripStatus{" +
                "deliveryAddress='" + deliveryAddress + '\'' +
                ", comments='" + comments + '\'' +
                ", status='" + status + '\'' +
                ", d_lat='" + d_lat + '\'' +
                ", d_long='" + d_long + '\'' +
                '}';
    }
}
