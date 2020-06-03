package com.deveint.user.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Kyra on 1/11/2016.
 */
public class PlacePredictions implements Serializable{

    public String strSourceLatitude = "";
    public String strSourceLongitude = "";
    public String strSourceLatLng = "";
    public String strSourceAddress = "";

    public String strDestLatitude = "";
    public String strDestLongitude = "";
    public String strDestLatLng = "";
    public String strDestAddress = "";

    public ArrayList<PlaceAutoComplete> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceAutoComplete> places) {
        this.predictions = places;
    }

    private ArrayList<PlaceAutoComplete> predictions;

    @Override
    public String toString() {
        return "PlacePredictions{" +
                "strSourceLatitude='" + strSourceLatitude + '\'' +
                ", strSourceLongitude='" + strSourceLongitude + '\'' +
                ", strSourceLatLng='" + strSourceLatLng + '\'' +
                ", strSourceAddress='" + strSourceAddress + '\'' +
                ", strDestLatitude='" + strDestLatitude + '\'' +
                ", strDestLongitude='" + strDestLongitude + '\'' +
                ", strDestLatLng='" + strDestLatLng + '\'' +
                ", strDestAddress='" + strDestAddress + '\'' +
                ", predictions=" + predictions +
                '}';
    }
}
