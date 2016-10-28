package com.example.coolweather.activity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.XmlParserHandler;
import com.example.coolweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseAreaActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private List<String> dataList = new ArrayList<String>();
	
	//省列表
	private List<Province> provinceList;
	
	//市列表
	private List<City> cityList;
	
	//当前选中的级别
	private int currentLevel;
	
	private TextView titleText;
	
	private CoolWeatherDB coolWeatherDB;
	
	private ProgressDialog progressDialog;

	//县列表
	private List<County> countyList;
		
	//选中的省份
	private Province selectedProvince;
	
	//选中的市
	private City selectedCity;
	
	//当前省的名称
	//private String mCurrentProviceName;
	
	//判断是否从  WeatherActivity中跳转过来
	private boolean isFromWeatherActivity;
	
	//存储城市名称
	public String city_master;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
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
	    listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0,
					View view, int index, long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);	
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					city_master = cityList.get(index).getCityName();
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyName = countyList.get(index).getCountyName();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("city_name", countyName);
					intent.putExtra("city_master", city_master);
					startActivity(intent);
					finish();
				}
			}
	    	
	    });
	    queryProvinces();
	}
	
	/*
	 * 查询全国所有的省，优先从数据库查询,如果没有查询到再去服务器上查询.
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() <= 0) {
			showProgressDialog();
			List<Province> provinceList = null;
			List<City> cityList = null;
			List<County> countyList = null;
			AssetManager asset = getAssets();
			try {
				InputStream input = asset.open("province_data.xml");
				//创建一个解析xml的工厂对象
				SAXParserFactory spf = SAXParserFactory.newInstance();
				//解析xml
				SAXParser parser = spf.newSAXParser();
				XmlParserHandler handler = new XmlParserHandler();
				parser.parse(input, handler);
				input.close();
				//获取解析出来的数据  Province
				provinceList = handler.getProvinceList();
				
				for (int i=0; i<provinceList.size(); i++) {
					//遍历所有的省的数据，并储存到数据库中
					Province province = new Province();
					province.setProvinceName(provinceList.get(i).getProvinceName());
					province.setProvinceCode(provinceList.get(i).getProvinceCode());
					//将解析出来的数据存储到Province表中
					coolWeatherDB.saveProvince(province);
				}
				
				//获取解析出来的数据 City
				cityList = handler.getCityList();
				for (int i=0; i<cityList.size(); i++) {
					//遍历所有的市的数据，并储存到数据库中
					City city = new City();
					city.setCityName(cityList.get(i).getCityName());
					city.setCityCode(cityList.get(i).getCityCode());
					city.setProvinceId(cityList.get(i).getProvinceId());
					//将解析出来的数据存储到City表中
					coolWeatherDB.saveCity(city);
				}
				
				//获取解析出来的数据 County
				countyList = handler.getCountyList();	
				for (int i=0; i<countyList.size(); i++) {
					//遍历所有的县的数据，并储存到数据库中
					County county = new County();
					county.setCountyName(countyList.get(i).getCountyName());
					county.setCityId(countyList.get(i).getCityId());
					//将解析出来的数据存储到County表中
					coolWeatherDB.saveCounty(county);
				}
				
				closeProgressDialog();
			} catch (Throwable e) {
				closeProgressDialog();
				e.printStackTrace();
			} finally {
			}
		}
		
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
		}
		
		for (Province province : provinceList) {
			dataList.add(province.getProvinceName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText("中国");
		currentLevel = LEVEL_PROVINCE;
		
	}
	
	/*
	 * 查询选中省内所有的市,优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(Integer.parseInt(selectedProvince.getProvinceCode()));
		if (cityList.size() > 0) {
		   dataList.clear();
		}
		
		for (City city : cityList) {
			dataList.add(city.getCityName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedProvince.getProvinceName());
		currentLevel = LEVEL_CITY;
	}
	
	/*
	 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	private void queryCounties() {	
		countyList = coolWeatherDB.loadCounties(selectedCity.getCityCode());
		if (countyList.size() > 0) {
		   dataList.clear();
		}
		
		for (County county : countyList) {
			dataList.add(county.getCountyName());
		}
		adapter.notifyDataSetChanged();
		listView.setSelection(0);
		titleText.setText(selectedCity.getCityName());
		currentLevel = LEVEL_COUNTY;
	}
	
	//显示进度对话框
		private void showProgressDialog() {
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载...");
				progressDialog.setCanceledOnTouchOutside(false);
			}
		}
		
		//关闭对话框
		private void closeProgressDialog() {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
}
