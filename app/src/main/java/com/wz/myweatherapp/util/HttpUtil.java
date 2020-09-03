package com.wz.myweatherapp.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    /**
     * 传入请求地址 注册一个回调来处理服务器响应
     * @param address
     * @param callback
     */
    public static void sendOkHttpRequest(String address,Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);

    }
}
