package com.example.coolweather.service;

import com.example.coolweather.receiver.AutoUpdateReceiver;
import com.example.coolweather.util.BaiduWeather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
				Intent intent = new Intent();
				intent.putExtra("update_state", "update");
				intent.setAction("android.intent.action.update");
				sendBroadcast(intent);
			}
			
		}).start();
		
		
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int anHour = 60 * 1000; //这是8小时的毫秒数  8 * 60 * 60 * 1000
		long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
		Intent i = new Intent(this, AutoUpdateReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	//更新天气
	private void updateWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String cityName_1 = prefs.getString("city_name", "");
		String cityName_2 = prefs.getString("master_city", "");

		if (!cityName_1.equals(""))  {
			//WeatherActivity a = new WeatherActivity();
			BaiduWeather.GetWeater(this, cityName_1, cityName_2);
			//a.refreshWeather.performClick();
		}
	}
	

}
