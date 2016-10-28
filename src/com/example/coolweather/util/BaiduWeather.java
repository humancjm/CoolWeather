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
	//获取天气信息
	public static String GetWeater(Context context, String city, String city_master) {
		currentCity = city;
		MasterCity = city_master;
		BaiduWeather wu = new BaiduWeather();
		String buffstr = null;
		try {
			String xml = wu.GetXmlCode(URLEncoder.encode(city, "utf-8")); //设置输入城市的编码,以满足百度天气API需要
			buffstr = wu.readStringXml(context, xml, city); //调用xml解析函数
			if (buffstr.equals("No data!")) {
				xml = wu.GetXmlCode(URLEncoder.encode(city_master, "utf-8")); //设置输入城市的编码,以满足百度天气API需要
				buffstr = wu.readStringXml(context, xml, city_master); //调用xml解析函数
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
			//建立连接
			URL url = new URL(requestUrl);
			HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoInput(true);
			httpUrlConn.setRequestMethod("GET");
			
			//获取输入流
			InputStream inputStream = httpUrlConn.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			//读取返回结果
			buffer = new StringBuffer();
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				buffer.append(str);
			}
			
			//释放资源
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			httpUrlConn.disconnect();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return buffer.toString(); //返回xml字符串
	}
	
	public String readStringXml(Context context,String xml, String ifcity) {
		StringBuffer buff = new StringBuffer(); //用来拼接天气信息
		Document doc = null;
		List<?> listdate = null; //用来存放日期
		List<?> listday = null; //用来存放白天图片路径信息
		List<?> listnightday = null; //用来存放晚上图片路径信息
		List<?> listweather = null;
		List<?> listwind = null;
		List<?> listtem = null;
		
		try {
			//读取并解析XML文档
			//下面是通过解析XML字符串
			doc = DocumentHelper.parseText(xml); //将字符串转为XML
			Element rootElt = doc.getRootElement(); //获取根节点
			Iterator<?> iter = rootElt.elementIterator("results"); //获取跟节点下的子节点 results
			String status = rootElt.elementText("status"); //获取状态，如果等于success，表示有数据
			if (!status.equals("success")) {
				saveWeatherInfo(context, currentCity, "", "", "","", "", "No data!","");
				return "No data!"; //如果不存在数据,直接返回
			}
			//String date = rootElt.elementText("date"); //获取根节点下的，当天日期.
			//buff.append(date + "\n");
			
			//遍历results节点
			while (iter.hasNext()) {
				Element recordEle = (Element) iter.next();
				Iterator<?> iters = recordEle.elementIterator("weather_data"); //遍历results节点下的 weather_data 节点
				while (iters.hasNext()) {
					Element itemEle = (Element) iters.next();
					listdate = itemEle.elements("date"); //将date集合放到listdate中
					listday = itemEle.elements("dayPictureUrl");
					listnightday = itemEle.elements("nightPictureUrl");
					listweather = itemEle.elements("weather");
					listwind = itemEle.elements("wind");
					listtem = itemEle.elements("temperature");
				}
				
				//for (int i=0;i<listdate.size();i++) { //由于每一个list.size都相等，这里统一处理
				for (int i=0;i<1;i++) { //由于每一个list.size都相等，这里统一处理
					Element eledate = (Element)listdate.get(i);
					Element eleday = (Element)listday.get(i);
					Element eleweather = (Element)listweather.get(i);
					Element elewind = (Element)listwind.get(i);
					Element eletem = (Element)listtem.get(i);
					Element elenightday = (Element) listnightday.get(i);
					
					//buff.append(eledate.getText()+" "+eleweather.getText()); //拼接信息
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
	
	//将天气信息存储到SharedPreferences文件中
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
