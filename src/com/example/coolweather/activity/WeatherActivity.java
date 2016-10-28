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
	
	//��ʾ��������
	private TextView cityNameText;
	
	//������ʾ����ʱ��
	private TextView publishText;
	
	//������ʾ����������Ϣ
	private TextView weatherDespText;
	
	//������ʾ����1
	private TextView temp1Text;
	
	//������ʾwind
	private TextView windText;
	
	//������ʾ��ǰ����
	private TextView currentDataText;
	
	//�л����а�ť
	private Button switchCity;
	
	//����������ť
	public Button refreshWeather;
	
	public MyReceiver receiver;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		//��ʼ�����ؼ�
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
		
			//���ؼ�����ʱ��ȥ��ѯ����
		    publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//��ѯ����
			publishText.setText("��ѯ����...");
			BaiduWeather.GetWeater(this, city_name, city_master);
			showWeather();
		} else {
			publishText.setText("����δ����...");
			Toast.makeText(this, "����δ���ӣ�", Toast.LENGTH_SHORT).show();
		}
		
		//ע�������
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.update");
		WeatherActivity.this.registerReceiver(receiver, filter);
		
			
		//} else {
			//û���ؼ�����ʱ��ֱ����ʾ��������
			//publishText.setText("û���ؼ�����ֱ����ʾ��������...");
		//	showWeather();
		//}
	}
	
	public class MyReceiver extends BroadcastReceiver {

		//�Զ���һ���㲥������
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
			//���캯������ʼ��
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
				publishText.setText("ͬ����...");
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				String cityName = prefs.getString("city_name", "");
				String city_master = prefs.getString("city_master", "");
				BaiduWeather.GetWeater(this, cityName, city_master);
				showWeather();
			} else {
				publishText.setText("����δ����...");
				Toast.makeText(this, "����δ���ӣ�", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}
	
	// ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	public void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		windText.setText(prefs.getString("wind", ""));
		weatherDespText.setText(prefs.getString("weather_code", ""));
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());       
		String str = formatter.format(new Date(System.currentTimeMillis()));  //��ȡ��ǰʱ��
		publishText.setText("����: " + str + "����");
		currentDataText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
	
	//�������
	public static boolean checkNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			//�������ж������Ƿ�����
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
		//���ע�������
		WeatherActivity.this.unregisterReceiver(receiver);
	}
	
	
}
