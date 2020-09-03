package com.wz.myweatherapp;

import android.app.ProgressDialog;
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
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long idl) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(pos);
                    queryCity();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(pos);
                    queryCounty();
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
     */
    private void queryProvince() {
          mTitleText.setText("中国");
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
    查询市内所有县
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
