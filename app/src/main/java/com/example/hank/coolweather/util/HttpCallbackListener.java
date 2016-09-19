package com.example.hank.coolweather.util;

/**
 * Created by Administrator on 2016/9/18.
 */
public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
