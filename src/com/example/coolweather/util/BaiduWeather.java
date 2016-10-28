package com.example.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;


public class BaiduWeather {
	
	public static String currentCity = "";
	
	public static String MasterCity = "";
	//��ȡ������Ϣ
	public static String GetWeater(Context context, String city, String city_master) {
		currentCity = city;
		MasterCity = city_master;
		BaiduWeather wu = new BaiduWeather();
		String buffstr = null;
		try {
			String xml = wu.GetXmlCode(URLEncoder.encode(city, "utf-8")); //����������еı���,������ٶ�����API��Ҫ
			buffstr = wu.readStringXml(context, xml, city); //����xml��������
			if (buffstr.equals("No data!")) {
				xml = wu.GetXmlCode(URLEncoder.encode(city_master, "utf-8")); //����������еı���,������ٶ�����API��Ҫ
				buffstr = wu.readStringXml(context, xml, city_master); //����xml��������
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffstr;
	}
	
	public String GetXmlCode(String city) throws UnsupportedEncodingException {
		String requestUrl = "http://api.map.baidu.com/telematics/v3/weather?location="
	+city+"&output=XML&ak=lECfSn3no1GPwGIKeKKGG0kAyPhbQ1l8";
		StringBuffer buffer = null;
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());  
        
		try {
			//��������
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoInput(true);
			httpUrlConn.setRequestMethod("GET");
			
			//��ȡ������
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			//��ȡ���ؽ��
			buffer = new StringBuffer();
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			
			//�ͷ���Դ
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			httpUrlConn.disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString(); //����xml�ַ���
	}
	
	public String readStringXml(Context context,String xml, String ifcity) {
		StringBuffer buff = new StringBuffer(); //����ƴ��������Ϣ
		Document doc = null;
		List<?> listdate = null; //�����������
		List<?> listday = null; //������Ű���ͼƬ·����Ϣ
		List<?> listnightday = null; //�����������ͼƬ·����Ϣ
		List<?> listweather = null;
		List<?> listwind = null;
		List<?> listtem = null;
		
		try {
			//��ȡ������XML�ĵ�
			//������ͨ������XML�ַ���
			doc = DocumentHelper.parseText(xml); //���ַ���תΪXML
			Element rootElt = doc.getRootElement(); //��ȡ���ڵ�
			Iterator<?> iter = rootElt.elementIterator("results"); //��ȡ���ڵ��µ��ӽڵ� results
			String status = rootElt.elementText("status"); //��ȡ״̬���������success����ʾ������
			if (!status.equals("success")) {
				saveWeatherInfo(context, currentCity, "", "", "","", "", "No data!","");
				return "No data!"; //�������������,ֱ�ӷ���
			}
			//String date = rootElt.elementText("date"); //��ȡ���ڵ��µģ���������.
			//buff.append(date + "\n");
			
			//����results�ڵ�
			while (iter.hasNext()) {
				Element recordEle = (Element) iter.next();
				Iterator<?> iters = recordEle.elementIterator("weather_data"); //����results�ڵ��µ� weather_data �ڵ�
				while (iters.hasNext()) {
					Element itemEle = (Element) iters.next();
					listdate = itemEle.elements("date"); //��date���Ϸŵ�listdate��
					listday = itemEle.elements("dayPictureUrl");
					listnightday = itemEle.elements("nightPictureUrl");
					listweather = itemEle.elements("weather");
					listwind = itemEle.elements("wind");
					listtem = itemEle.elements("temperature");
				}
				
				//for (int i=0;i<listdate.size();i++) { //����ÿһ��list.size����ȣ�����ͳһ����
				for (int i=0;i<1;i++) { //����ÿһ��list.size����ȣ�����ͳһ����
					Element eledate = (Element)listdate.get(i);
					Element eleday = (Element)listday.get(i);
					Element eleweather = (Element)listweather.get(i);
					Element elewind = (Element)listwind.get(i);
					Element eletem = (Element)listtem.get(i);
					Element elenightday = (Element) listnightday.get(i);
					
					//buff.append(eledate.getText()+" "+eleweather.getText()); //ƴ����Ϣ
					saveWeatherInfo(context, currentCity, eledate.getText(), eleday.getText(), elenightday.getText(),
							eleweather.getText(), elewind.getText(), eletem.getText(), MasterCity);
				}			
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return buff.toString();
	}
	
	//��������Ϣ�洢��SharedPreferences�ļ���
	public static void saveWeatherInfo(Context context, String cityName, String currentDate, String day_pic, String niday_pic,
			String weather, String wind, String temp1, String city_master) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("current_date", currentDate);
		editor.putString("weather_code", weather);
		editor.putString("wind", wind);
		editor.putString("temp1", temp1);
		editor.putString("day_pic", day_pic);
		editor.putString("niday_pic", niday_pic);
		editor.putString("city_master", city_master);
		editor.commit();
	}

}
