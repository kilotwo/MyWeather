package com.wz.myweatherapp.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;
/*
在 Weather类中,我们对 Basic、AQI、NoW、 Suggestion和 Forecast类进行了引用。其
中,由于 daily forecast中包含的是一个数组,因此这里使用了List集合来引用 Forecast类。

 */
public class Weather {
    /*
    另外,返回的天气数据中还会包含一项 status数据,成功返回ok,失败则会返回具体的原因,那
    么这里也需要添加一个对应的 status字段
    现在所有的GSON实体类都定义好了,接下来我们开始编写天气界面。
     */
    public String status;
    public Basic Basic;
    public AQI AQI;
    public Now Now;
    public Suggestion Suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> mForecastList;
}
