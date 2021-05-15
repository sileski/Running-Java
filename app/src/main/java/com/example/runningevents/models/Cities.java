package com.example.runningevents.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Cities {

    @SerializedName("error")
    private String error;

    @SerializedName("data")
    private ArrayList<String> data;

    public String getError() {
        return error;
    }

    public ArrayList<String> getData() {
        return data;
    }
}
