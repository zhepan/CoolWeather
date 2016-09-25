package com.example.hank.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hank.coolweather.R;
import com.example.hank.coolweather.db.CoolWeatherDB;
import com.example.hank.coolweather.model.City;
import com.example.hank.coolweather.model.Province;
import com.example.hank.coolweather.util.HttpCallbackListener;
import com.example.hank.coolweather.util.HttpUtil;
import com.example.hank.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/18.
 */
public class ChooseAreaActivity extends Activity {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;

    private ListView listView;
    private TextView titleText;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<String>();
    private CoolWeatherDB coolWeatherDB;
    private int currentLevel;
    private List<Province> provinceList;
    private List<City> cityList;
    private Province selectedProvince;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        /* If city has been selected before, but not back from WeatherActivity */
        if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    String cityCode = cityList.get(position).getCityCode();
                    Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                    intent.putExtra("city_code", cityCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        queryProvince();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void queryProvince() {
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size() > 0) {
            /* Clear dataList*/
            dataList.clear();
            for (Province province: provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer();
        }
    }

    private void queryCities() {
        cityList = coolWeatherDB.loadCity(selectedProvince.getProvinceName());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city: cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer();
        }
    }

    private void queryFromServer() {
        String address = "https://api.heweather.com/x3/citylist?search=allchina&key=5be34865773f4d97ad294ecfee85b75d";
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if (Utility.handleCityListResponse(coolWeatherDB, response)) {
                    /* onFinish executed in child thread, we should switch to main thread to update UI*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            queryProvince();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "Load fail!", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_CITY) {
            queryProvince();
        } else {
            finish();
        }
    }
}
