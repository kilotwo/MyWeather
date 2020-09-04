package com.wz.myweatherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.wz.myweatherapp.db.City;
import com.wz.myweatherapp.db.County;
import com.wz.myweatherapp.db.Province;
import com.wz.myweatherapp.util.HttpUtil;
import com.wz.myweatherapp.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import okhttp3.Address;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY =2;
    private List<String>dataList = new ArrayList<>();
    private TextView mTitleText;
    private Button mButton;
    private ListView mListView;
    private ArrayAdapter<String> adapter;
    private ProgressDialog mProgressDialog;
    /*
    选中的级别
     */
    private int currentLevel;
    /*
    选中的城市
     */
    private City selectedCity;
    /*
    选中的省份
     */
    private Province selectedProvince;
    private List<Province>provinceList;
    private List<City>cityList;
    private List<County>countyList;
    @Nullable
    @Override
     public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        mTitleText = view.findViewById(R.id.title_text);
        mButton = view.findViewById(R.id.back_button);
        mListView = view.findViewById(R.id.list_view);
        //不过,数组中的数据是无法直接传递给 Listview的,我们还需要借助适配器来完成。 Android
        //中提供了很多适配器的实现类,其中我认为最好用的就是 Array Adapter。它可以通过泛型来指定
        //要适配的数据类型,然后在构造函数中把要适配的数据传入。 Array Adapter有多个构造函数的重
        //载,你应该根据实际情况选择最合适的一种。这里如果提供的数据都是字符串,可以将
        //ArrayAdapter的泛型指定为 String
        // 然后在ArrayAdapter的构造函数中依次传入当前上下文，List view子项布局的id,以及要适配的数据。
        // 注意,使用了simple_list_item_1作为 List view子项布局的id,这是一个 Android内置的布局文件,里面只有一个
        //Text View,可用于简单地显示一段文本。这样适配器对象就构建好了
        //最后,还需要调用 List View的 setAdapter()方法,将构建好的适配器对象传递进去,这样
        //List view和数据之间的关联就建立完成了。
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*
        列表点击事件
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //可以看到,我们使用 setonItemClicklistener()方法为 Listview注册了一个监听器,当
            //用户点击了 Listview中的任何一个子项时,就会回调 onItemclick()方法。在这个方法中可以
            //通过 position参数判断出用户点击的是哪一个子项,然后获取到相应的类信息,并通过Toast显示
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long idl) {
                //当你点击了某个item的时候会进入到 List view的 onItemclick()方法中,这个时候会根据当
                //前的级别来判断是去调用 querycities()方法还是 query Counties()方法, queryCities()方
                //法是去査询市级数据,而 queryCounties()方法是去查询县级数据,这两个方法内部的流程和
                //queryProvinces()方法基本相同
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(pos);
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(pos);
                    queryCounty();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(pos).getWeatherId();
                    Intent intent = new Intent(getActivity(),WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        /*
        返回按钮 点击事件
         */
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){
                    /*
                    如果是在县页面返回 则获取市信息
                     */
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    /*
                    如果实在市级别返回 则获取省信息
                     */
                    queryProvince();
                }
            }
        });

        /*
        创建时默认获取省信息
         */
        //在 onActivityCreated()方法的最后,调用了 query provinces()方法,也就是从这里开
        //始加载省级数据的。
        queryProvince();
    }
/*
查询省内所有市
 */
    private void queryCity() {
            mTitleText.setText(selectedProvince.getProvinceName());
            mButton.setVisibility(View.VISIBLE);
            cityList = DataSupport.where("provinceid  = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0) {
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address ="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    /*
    查询全国所有省 优先从数据可查询 若无再去服务器查询
    query Provinces()方法中首先会将头布局的标题设置成中国,将返回按钮
    隐藏起来,因为省级列表已经不能再返回了。然后调用 LiteRal的查询接口来从数据库中读取省
    级数据,如果读取到了就直接将数据显示到界面上,如果没有读取到组装出一个请求地址,
    然后调用 queryFromServer()方法来从服务器上查询数据。
     */
    private void queryProvince() {
          mTitleText.setText("中国");
          //另外还有一点需要注意,在返回按钮的点击事件里,会对当前 List view的列表级别进行判断。
        //如果当前是县级列表,那么就返回到市级列表,如果当前是市级列表,那么就返回到省级表列表。
        //当返回到省级列表时,返回按钮会自动隐藏,从而也就不需要再做进一步的处理了。
          mButton.setVisibility(View.GONE);
          provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * query Fromserver()方法中会调用 HttpUtil的send0httPrequest()方法来向服务器发送
     * 请求,响应的数据会回调到 onResponse()方法中,然后我们在这里去调用 Utility的
     * handleprovincesresponse()方法来解析和处理服务器返回的数据,并存储到数据库中。接下
     * 来的一步很关键,在解析和处理完数据之后,再次调用了 queryProvinces()方法来重新加
     * 载省级数据,由于 queryProvinces()方法牵扯到了U操作,因此必须要在主线程中调用,这
     * 里借助了 runonuiThread()方法来实现从子线程切换到主线程。现在数据库中已经存在了数据
     * 因此调用 queryProvinces()就会直接将数据显示到界面上了
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //我们可以使用如下写法来得到返回的具体内容:
                    String responseText = response.body().string();
                    boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if ("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvince();
                            }else if ("city".equals(type)){
                                queryCity();
                            }else if ("county".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });

    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
            mProgressDialog.show();
    }

    /*
    查询市内所有区/县
     */
    private void queryCounty() {
        mTitleText.setText(selectedCity.getCityName());
        mButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0) {
            dataList.clear();
            for (County county:countyList
                 ) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel =LEVEL_COUNTY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode +"/"+cityCode;
            queryFromServer(address,"county");
        }
    }


}
