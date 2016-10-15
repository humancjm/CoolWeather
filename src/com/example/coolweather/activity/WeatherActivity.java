package com.example.coolweather.activity;

import com.example.coolweather.R;
import com.example.coolweather.util.BaiduWeather;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
	
	//������ʾ����2
	private TextView temp2Text;
	
	//������ʾ��ǰ����
	private TextView currentDataText;
	
	//�л����а�ť
	private Button switchCity;
	
	//����������ť
	private Button refreshWeather;

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
			publishText.setText("ͬ����...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String cityName = prefs.getString("city_name", "");
			if (!TextUtils.isEmpty(cityName)) {
				BaiduWeather.GetWeater(this, cityName);
				showWeather();
			}
			break;
		}
	}

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
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDataText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		String city_name = getIntent().getStringExtra("city_name");
		
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
		if (!TextUtils.isEmpty(city_name)) {
			//���ؼ�����ʱ��ȥ��ѯ����
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//��ѯ����
			publishText.setText("��ѯ����...");
			BaiduWeather.GetWeater(this, city_name);
			showWeather();
		} else {
			//û���ؼ�����ʱ��ֱ����ʾ��������
			//publishText.setText("û���ؼ�����ֱ����ʾ��������...");
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	// ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		//publishText.setText("����" + prefs.getString("publish_time", "") + "����");
		currentDataText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		//Intent intent = new Intent(this, AutoUpdateService.class);
		//startService(intent);
	}
	
	

}
