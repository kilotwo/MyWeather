package com.wz.myweatherapp.gson;

import com.google.gson.annotations.SerializedName;

public class Now  {
    @SerializedName("tmp")
    public String tmp;
    @SerializedName("cond")
    public More More;
    public class More{
        @SerializedName("txt")
        public String info;

    }
}
