package com.wz.myweatherapp.db;

import org.litepal.crud.DataSupport;

/**
 * 城市信息表
 */
public class City extends DataSupport {
    private int id; //字段
    private String cityName; //城市名称
    private int cityCode;   //城市代码
    private int provinceId;//城市所属省份编号


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

}
