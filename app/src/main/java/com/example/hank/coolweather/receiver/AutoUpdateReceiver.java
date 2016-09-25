package com.example.hank.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.hank.coolweather.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/9/26.
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /* Receiver the broadcast every 8 hours, then start the services again */
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
