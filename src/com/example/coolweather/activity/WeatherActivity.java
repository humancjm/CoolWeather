package com.example.coolweather.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.coolweather.R;
import com.example.coolweather.service.AutoUpdateService;
import com.example.coolweather.util.BaiduWeather;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherInfoLayout;
	
	//显示城市名称
	private TextView cityNameText;
	
	//用于显示发布时间
	private TextView publishText;
	
	//用于显示天气描述信息
	private TextView weatherDespText;
	
	//用于显示气温1
	private TextView temp1Text;
	
	//用于显示wind
	private TextView windText;
	
	//用于显示当前日期
	private TextView currentDataText;
	
	//切换城市按钮
	private Button switchCity;
	
	//更新天气按钮
	public Button refreshWeather;
	
	public MyReceiver receiver;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		//初始化各控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		windText = (TextView) findViewById(R.id.wind);
		currentDataText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String city_name = getIntent().getStringExtra("city_name");
		String city_master = getIntent().getStringExtra("city_master");
	
		if (null != city_name && !"".equals(city_name)) { 
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editor.putString("city_name", city_name);
			editor.putString("city_master", city_master);
			editor.commit();
		}
		switchCity.setOnClickListener(this);
		
		refreshWeather.setOnClickListener(this);
		
		//if (!TextUtils.isEmpty(city_name)) {
		if (checkNetworkAvailable(this) == true) {
		
			//有县级代号时就去查询天气
		    publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//查询天气
			publishText.setText("查询天气...");
			BaiduWeather.GetWeater(this, city_name, city_master);
			showWeather();
		} else {
			publishText.setText("网络未连接...");
			Toast.makeText(this, "网络未连接！", Toast.LENGTH_SHORT).show();
		}
		
		//注册接收器
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.update");
		WeatherActivity.this.registerReceiver(receiver, filter);
		
			
		//} else {
			//没有县级代号时就直接显示本地天气
			//publishText.setText("没有县级代号直接显示本地天气...");
		//	showWeather();
		//}
	}
	
	public class MyReceiver extends BroadcastReceiver {

		//自定义一个广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (checkNetworkAvailable(context) == true) {
				Bundle bundle = intent.getExtras();
				if (bundle.getString("update_state").equals("update")) {
					showWeather();
				}
			} 
		}
		
		public MyReceiver() {
			//构造函数，初始化
		}		
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			if (checkNetworkAvailable(this) == true) {
				publishText.setText("同步中...");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String cityName = prefs.getString("city_name", "");
				String city_master = prefs.getString("city_master", "");
				BaiduWeather.GetWeater(this, cityName, city_master);
				showWeather();
			} else {
				publishText.setText("网络未连接...");
				Toast.makeText(this, "网络未连接！", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}
	
	// 从SharedPreferences文件中读取存储的天气信息，并显示到界面上。
	public void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		windText.setText(prefs.getString("wind", ""));
		weatherDespText.setText(prefs.getString("weather_code", ""));
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());       
		String str = formatter.format(new Date(System.currentTimeMillis()));  //获取当前时间
		publishText.setText("今天: " + str + "发布");
		currentDataText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
	//检查网络
	public static boolean checkNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			//仅仅是判断网络是否连接
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
 				}
			}
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//解除注册接收器
		WeatherActivity.this.unregisterReceiver(receiver);
	}
	
	
}
