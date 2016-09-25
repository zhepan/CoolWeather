package com.example.hank.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

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
    private static final String HE_FENG_WEATHER_INFO_HEADER="HeWeather data service 3.0";

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

    public static void handleWeatherInfoResponse(Context context, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                Log.d("Res", "response=" + response);
                String[] resArray1 = response.split("daily_forecast");
                String[] resArray2 = resArray1[1].split("now");

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append(resArray1[0]).append("now").append(resArray2[1]);

                JSONObject jsonObject = new JSONObject(responseBuilder.toString());
                JSONArray jsonArray = jsonObject.getJSONArray(HE_FENG_WEATHER_INFO_HEADER);
                /* The weather info array length is 1 */
                JSONObject weatherObj = jsonArray.getJSONObject(0);

                /* aqi info */
                JSONObject aqiInfoObj = weatherObj.getJSONObject("aqi");
                JSONObject apiInfoObjL2 = aqiInfoObj.getJSONObject("city");
                String aqi = apiInfoObjL2.getString("aqi");
                String pm25 = apiInfoObjL2.getString("pm25");
                String qlty = apiInfoObjL2.getString("qlty");

                /* status */

                /* now */
                JSONObject nowInfoObj = weatherObj.getJSONObject("now");
                String tmp = nowInfoObj.getString("tmp");
                String condInfo = nowInfoObj.getString("cond");
                JSONObject condObj = new JSONObject(condInfo);
                String weatherTxt = condObj.getString("txt");
                String windInfo = nowInfoObj.getString("wind");
                JSONObject windObj = new JSONObject(windInfo);
                String windDir = windObj.getString("dir");

                /* basic info */
                JSONObject basicInfoObj = weatherObj.getJSONObject("basic");
                String city = basicInfoObj.getString("city");
                String cityCode = basicInfoObj.getString("id");
                String updateInfo = basicInfoObj.getString("update");
                JSONObject updateObj = new JSONObject(updateInfo);
                String locTime = updateObj.getString("loc");

                /* suggestion */
                if (response.contains("suggestion")) {
                    JSONObject suggestionObj = weatherObj.getJSONObject("suggestion");
                    String comf = suggestionObj.getString("comf");
                    JSONObject comfObj = new JSONObject(comf);
                    String suggestionTxt = comfObj.getString("txt");
                    saveWeatherInfo(context, aqi, pm25, qlty, city, cityCode, locTime,
                            tmp, weatherTxt, windDir, suggestionTxt);
                }

                saveWeatherInfo(context, aqi, pm25, qlty, city, cityCode, locTime,
                        tmp, weatherTxt, windDir, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveWeatherInfo(Context context, String aqi, String pm25, String qlty,
                                       String city, String cityCode, String locTime, String tmp, String weatherTxt,
                                       String windDir, String suggestion) {
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(context).edit();

        /* If city has been selected, next time enter weather activity directly */
        editor.putBoolean("city_selected", true);
        editor.putString("aqi", aqi);
        editor.putString("pm25", pm25);
        editor.putString("qlty", qlty);
        editor.putString("city", city);
        Log.d("city_codexx", cityCode);
        editor.putString("city_code", cityCode);
        editor.putString("locTime", locTime);
        editor.putString("tmp", tmp);
        editor.putString("weatherTxt", weatherTxt);
        editor.putString("winDir", windDir);
        editor.putString("suggestion", suggestion);
        editor.commit();
    }
}
