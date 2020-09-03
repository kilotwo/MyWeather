package com.wz.myweatherapp.util;

import android.text.TextUtils;

import com.wz.myweatherapp.db.City;
import com.wz.myweatherapp.db.County;
import com.wz.myweatherapp.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {
    /**
     * 解析处理服务器返回的省级数据
     *
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                //先使用JSONArray 和 JSONObject将数据解析
                JSONArray allProvince = new JSONArray(response);
                for (int i = 0; i < allProvince.length(); i++) {
                    JSONObject provinceObject = allProvince.getJSONObject(i);
                    //装入实体对象
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    //由于province 继承了litepal特性 故使用save存储进数据库
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
/**
 * 解析处理服务器返回的市级数据
 */
public static boolean handleCityResponse(String response,int provinceId){
    try {
        if (!TextUtils.isEmpty(response)) {
            JSONArray allCities = new JSONArray(response);
            for (int i = 0; i < allCities.length(); i++) {
                JSONObject cityObject = allCities.getJSONObject(i);
                City city = new City();
                city.setCityName(cityObject.getString("name"));
                city.setCityCode(cityObject.getInt("id"));
                city.setProvinceId(provinceId);
                city.save();
            }
            return true;
        }
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return false;
}
/**
 * 解析处理县级数据
 *
 */
public static boolean handleCountyResponse(String response ,int cityId){
    try {
        if (!TextUtils.isEmpty(response)) {
            JSONArray allCounties = new JSONArray(response);
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject countyObject = allCounties.getJSONObject(i);
                County county  = new County();
                county.setCityId(cityId);
                county.setCountyName(countyObject.getString("name"));
                county.setWeatherId(countyObject.getString("weather_id"));
                county.save();
            }
            return true;
        }
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return false;
}






}
