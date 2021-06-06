package com.example.runningevents.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Location {

    @SerializedName("data")
    private List<LocationData> data;

    public void setData(List<LocationData> data){
        this.data = data;
    }
    public List<LocationData> getData(){
        return this.data;
    }

    public static class LocationData{

        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

}
