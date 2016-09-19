package com.example.hank.coolweather.util;

import android.text.TextUtils;

import com.example.hank.coolweather.db.CoolWeatherDB;
import com.example.hank.coolweather.model.City;
import com.example.hank.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashSet;

/**
 * Created by Administrator on 2016/9/18.
 */
public class Utility {
    public static boolean handleCityListResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String cityInfoArray = jsonObject.getString("city_info");
                JSONArray jsonArray = new JSONArray(cityInfoArray);
                LinkedHashSet<String> provinceNameSet = new LinkedHashSet();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject cityInfo = jsonArray.getJSONObject(i);
                    String cityName = cityInfo.getString("city");
                    String cityCode = cityInfo.getString("id");
                    String provinceName = cityInfo.getString("prov");

                    provinceNameSet.add(provinceName);

                    City city = new City();
                    city.setCityCode(cityCode);
                    city.setCityName(cityName);
                    city.setProvinceName(provinceName);
                    coolWeatherDB.saveCity(city);
                }

                for (String provName : provinceNameSet) {
                    Province province = new Province();
                    province.setProvinceName(provName);
                    coolWeatherDB.saveProvince(province);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }
}
