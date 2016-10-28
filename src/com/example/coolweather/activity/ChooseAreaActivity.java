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
	
	//ʡ�б�
	private List<Province> provinceList;
	
	//���б�
	private List<City> cityList;
	
	//��ǰѡ�еļ���
	private int currentLevel;
	
	private TextView titleText;
	
	private CoolWeatherDB coolWeatherDB;
	
	private ProgressDialog progressDialog;

	//���б�
	private List<County> countyList;
		
	//ѡ�е�ʡ��
	private Province selectedProvince;
	
	//ѡ�е���
	private City selectedCity;
	
	//��ǰʡ������
	//private String mCurrentProviceName;
	
	//�ж��Ƿ��  WeatherActivity����ת����
	private boolean isFromWeatherActivity;
	
	//�洢��������
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
	 * ��ѯȫ�����е�ʡ�����ȴ����ݿ��ѯ,���û�в�ѯ����ȥ�������ϲ�ѯ.
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
				//����һ������xml�Ĺ�������
				SAXParserFactory spf = SAXParserFactory.newInstance();
				//����xml
				SAXParser parser = spf.newSAXParser();
				XmlParserHandler handler = new XmlParserHandler();
				parser.parse(input, handler);
				input.close();
				//��ȡ��������������  Province
				provinceList = handler.getProvinceList();
				
				for (int i=0; i<provinceList.size(); i++) {
					//�������е�ʡ�����ݣ������浽���ݿ���
					Province province = new Province();
					province.setProvinceName(provinceList.get(i).getProvinceName());
					province.setProvinceCode(provinceList.get(i).getProvinceCode());
					//���������������ݴ洢��Province����
					coolWeatherDB.saveProvince(province);
				}
				
				//��ȡ�������������� City
				cityList = handler.getCityList();
				for (int i=0; i<cityList.size(); i++) {
					//�������е��е����ݣ������浽���ݿ���
					City city = new City();
					city.setCityName(cityList.get(i).getCityName());
					city.setCityCode(cityList.get(i).getCityCode());
					city.setProvinceId(cityList.get(i).getProvinceId());
					//���������������ݴ洢��City����
					coolWeatherDB.saveCity(city);
				}
				
				//��ȡ�������������� County
				countyList = handler.getCountyList();	
				for (int i=0; i<countyList.size(); i++) {
					//�������е��ص����ݣ������浽���ݿ���
					County county = new County();
					county.setCountyName(countyList.get(i).getCountyName());
					county.setCityId(countyList.get(i).getCityId());
					//���������������ݴ洢��County����
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
		titleText.setText("�й�");
		currentLevel = LEVEL_PROVINCE;
		
	}
	
	/*
	 * ��ѯѡ��ʡ�����е���,���ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	 * ��ѯѡ���������е��أ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ��
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
	
	//��ʾ���ȶԻ���
		private void showProgressDialog() {
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("���ڼ���...");
				progressDialog.setCanceledOnTouchOutside(false);
			}
		}
		
		//�رնԻ���
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
