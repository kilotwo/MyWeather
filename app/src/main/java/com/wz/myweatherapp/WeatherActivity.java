package com.wz.myweatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.wz.myweatherapp.gson.Forecast;
import com.wz.myweatherapp.gson.Weather;
import com.wz.myweatherapp.service.AutoUpdateService;
import com.wz.myweatherapp.util.HttpUtil;
import com.wz.myweatherapp.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WeatherActivity extends AppCompatActivity {

    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mDegreeText;
    private TextView mWeatherInfoText;
    private LinearLayout mForecast;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mCarWashText;
    private TextView mSportText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout mSwipeRefreshLayout;

    public DrawerLayout mDrawerLayout;
    private Button navButton;

    private String mWeatherId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //接着我们调用了 getwindow(). getDecorview()方法拿到当前活动的 Decor view,再调用它
        //的 setSystemUiVisibility()方法来改变系统UI的显示,这里传入View. SYSTEM UI
        //FLAG LAYOUT FULLSCREEN和View. SYSTEM UI FLAG LAYOUT STABLE就表示活动的布局会显
        //在状态栏上面,最后调用一下 setstatus BarColor()方法将状态栏设置成透明色
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_weather);
        //初始化各控件
        mWeatherLayout = findViewById(R.id.weather_layout);
        mTitleCity = findViewById(R.id.title_city);
        mTitleUpdateTime = findViewById(R.id.title_update_time);
        mDegreeText = findViewById(R.id.degree_text);
        mWeatherInfoText = findViewById(R.id.weather_info_text);

        mForecast = findViewById(R.id.forecast_layout);
        mAqiText = findViewById(R.id.aqi_text);
        mPm25Text = findViewById(R.id.pm25_text);
        mComfortText = findViewById(R.id.comfort_text);
        mCarWashText = findViewById(R.id.car_wash_text);
        mSportText = findViewById(R.id.sport_text);

        bingPicImg = findViewById(R.id.bing_pic_img);


        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);


        mDrawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);



        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
//这个活动中的代码也比较长,我们还是一步步梳理下。在 oncreate()方法中仍然先是去获
//取一些控件的实例,然后会尝试从本地缓存中读取天气数据。那么第一次肯定是没有缓存的,因
//此就会从 Intent中取出天气id,并调用 requestWeather()方法来从服务器请求天气数据。注意,
//请求数据的时候先将 ScrollⅤview进行隐藏,不然空数据的界面看上去会很奇怪。
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        //----------------------------------------------------------------------

       // final String weatherId;
        if (weatherString != null){
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            //请求天气
            requestWeather(mWeatherId);
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }


    }
/*
loadBingPic()方法中的逻辑就非常简单了,先是调用了Httputil.sendokhttprequest()
方法获取到必应背景图的链接,然后将这个链接缓存到 SharedPreferences当中,再将当前线程切
换到主线程,最后使用 Glide来加载这张图片就可以了。另外需要注意,在 requestweather()
方法的最后也需要调用一下10 adBingPic()方法,这样在每次请求天气信息的时候同时也会刷
新背景图片。现在重新运行一下程序,效果如图1424所示。

 */
    private void loadBingPic() {
            String requestBingPic = "http://guolin.tech/api/bing_pic";
            HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String bingPic = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic",bingPic);
                    editor.apply();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        }
                    });
                }
            });
    }

    /*
    showweather Info()方法中的逻辑就比较简单了,其实就是从 Weather对象中获取数据
然后显示到相应的控件上。注意在未来几天天气预报的部分我们使用了一个for循环来处理每天
的天气信息,在循环中动态加载 forecast item.xml布局并设置相应的数据,然后添加到父布局当
中。设置完了所有数据之后,记得要将 Scrollview重新变成可见。
这样我们就将首次进入 WeatherActivity时的逻辑全部梳理完了,那么当下一次再进入
Weather Actiⅳvity时,由于缓存已经存在了,因此会直接解析并显示天气数据,而不会再次发起网
络请求了。

     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree =  weather.now.tmp+"°C";
        String weatherInfo = weather.now.More.info;
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime);
        mDegreeText.setText(degree);
        mWeatherInfoText.setText(weatherInfo);
        mForecast.removeAllViews();
        for (Forecast forecast:weather.mForecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,mForecast,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.Temperature.max);
            minText.setText(forecast.Temperature.min);
            mForecast.addView(view);
        }
        if (weather.aqi!=null){
            mAqiText.setText(weather.aqi.city.aqi);
            mPm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度" + weather.suggestion.comfort.info;
        String carwash = "洗车指数" +weather.suggestion.carWash.info;
        String sport = "运动建议" + weather.suggestion.sport.info;
        mComfortText.setText(comfort);
        mCarWashText.setText(carwash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
     }

    /*
    根据天气Id请求城市天气信息
    requestWeather()方法中先是使用了参数中传入的天气id和我们之前申请好的 API Key拼装
出一个接地址,接着调用Httputil.endokhttpreQuest()方法来向该地址发出请求,服务器
会将相应城市的天气信息以JSON格式返回。然后我们在 onResponse()回调中先调用 Utility
handleWeatherResponse()方法将返回的JSON数据转换成 Weather对象,再将当前线程切换到
主线程。然后进行判断,如果服务器返回的 status状态是ok,就说明请求天气成功了,此时将返
回的数据缓存到 SharedPreferences当中,并调用 showweatherinfo()方法来进行内容显示。

     */

    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId +"&key=755a053d247341699ebbe941099d994f";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null&&"ok".equals(weather.status)){

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);

                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气数据失败1", Toast.LENGTH_SHORT).show();

                        }
                        //另外不要忘记,当请求结束后,还需要调用 SwipeRefreshLayout的 setRefreshing()方法
                        //并传入 false,用于表示刷新事件结束,并隐藏刷新进度条。
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
            loadBingPic();
    }
}
