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
        //创建一个OkHttpClient实例
        OkHttpClient client = new OkHttpClient();
        //创建Request来发起HTTP请求
        Request request = new Request.Builder().url(address).build();
        //之后调用OkhttpClient的newCall()方法来创建一个CalL对象,并调用它的execute()方
        //法来发送请求并获取服务器返回的数据,写法如下
        client.newCall(request).enqueue(callback);

    }
}
