package com.example.hank.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hank.coolweather.R;
import com.example.hank.coolweather.service.AutoUpdateService;
import com.example.hank.coolweather.util.HttpCallbackListener;
import com.example.hank.coolweather.util.HttpUtil;
import com.example.hank.coolweather.util.Utility;

/**
 * Created by Administrator on 2016/9/22.
 */
public class WeatherActivity extends Activity {
    private TextView cityName;
    private TextView locTime;
    private TextView weatherTxt;
    private TextView tmp;
    private TextView windDir;
    private TextView aqiNum;
    private TextView pm25Num;
    private TextView qltyNum;
    private TextView suggestion;
    private LinearLayout weatherInfoLayout;
    private Button switchCity;
    private Button refreshWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_info);

        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityName = (TextView) findViewById(R.id.city_name);
        locTime  = (TextView) findViewById(R.id.loc_time);
        weatherTxt = (TextView) findViewById(R.id.weather_txt);
        tmp = (TextView) findViewById(R.id.tmp);
        windDir = (TextView) findViewById(R.id.wind_dir);
        aqiNum = (TextView) findViewById(R.id.aqi);
        pm25Num = (TextView) findViewById(R.id.pm25);
        qltyNum = (TextView) findViewById(R.id.qlty);
        suggestion = (TextView) findViewById(R.id.suggestion);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);

        switchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
            }
        });

        refreshWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryWeather(null);
            }
        });

        String cityCode = getIntent().getStringExtra("city_code");
        queryWeather(cityCode);

        /* Start auto update service */
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void queryWeather(String cityCodeFromIntent) {
        String cityCode = cityCodeFromIntent;

        if (cityCode == null) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(this);

            cityCode = pref.getString("city_code", "");
        }

        if (!TextUtils.isEmpty(cityCode)) {
            locTime.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherCode(cityCode);
        }
    }

    private void queryWeatherCode(String cityCode) {
        String address = "https://api.heweather.com/x3/weather?cityid=" + cityCode
                + "&key=5be34865773f4d97ad294ecfee85b75d";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                Utility.handleWeatherInfoResponse(WeatherActivity.this, response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        locTime.setText("同步失败，请检查网络");
                    }
                });
            }
        });
    }

    private void showWeather() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(pref.getString("city", ""));
        locTime.setText("更新时间: " + pref.getString("locTime", ""));
        weatherTxt.setText("天气: " + pref.getString("weatherTxt", ""));
        tmp.setText("温度: " + pref.getString("tmp", "") + "℃");
        windDir.setText("风向: " + pref.getString("winDir", ""));
        aqiNum.setText("空气质量指数: " + pref.getString("aqi", ""));
        pm25Num.setText("pm2.5: " + pref.getString("pm25", ""));
        qltyNum.setText(" " + pref.getString("qlty", ""));

        String suggestionText = pref.getString("suggestion", "");
        if (suggestionText != "") {
            suggestion.setText("建议: " + pref.getString("suggestion", ""));
        } else {
            suggestion.setVisibility(View.INVISIBLE);
        }

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);
    }
}
