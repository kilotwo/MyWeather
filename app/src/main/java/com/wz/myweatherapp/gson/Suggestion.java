package com.wz.myweatherapp.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")
    public Comfort Comfort;
    @SerializedName("cw")
    public CarWash CarWash;

    public Sport Sport;
    public class Sport{
        @SerializedName("txt")
    public String info;

    }
    public class CarWash{
        @SerializedName("txt")
    public String info;
    }
    public class Comfort{
        @SerializedName("txt")
        public  String info;
    }
}
