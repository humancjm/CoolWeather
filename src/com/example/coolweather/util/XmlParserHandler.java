package com.example.coolweather.util;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;

public class XmlParserHandler extends DefaultHandler {
	//�洢���еĽ�������
	private List<Province> provinceList = new ArrayList<Province>();
	private List<City> cityList = new ArrayList<City>();
	private List<County> countyList = new ArrayList<County>();
	Province province = new Province();
	City city = new City();
	County county = new County();
	
	public XmlParserHandler() {
		
	}
	
	public List<Province> getProvinceList() {
		return provinceList;
	}
	
	public List<City> getCityList() {
		return cityList;
	}
	
	public List<County> getCountyList() {
		return countyList;
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		//��������һ����ʼ��ǩ��ʱ�򣬻ᴥ���������
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		//��������ʼ��ǵ�ʱ�򣬵����������
		if (qName.equals("province")) {
			province = new Province();
			province.setProvinceName(attributes.getValue(0));
			province.setProvinceCode(attributes.getValue(1));
		} else if (qName.equals("city")) {
			city = new City();
			city.setCityName(attributes.getValue(0));
			city.setProvinceId(Integer.parseInt(attributes.getValue(1)));
			city.setCityCode(attributes.getValue(2));
		} else if (qName.equals("district")) {
			county = new County();
			county.setCountyName(attributes.getValue(0));
			county.setCityId(attributes.getValue(1));
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		//super.endElement(uri, localName, qName);
		//����������ǵ�ʱ�򣬻�����������
		if (qName.equals("district")) {
			countyList.add(county);
		} else if (qName.equals("city")) {
			cityList.add(city);
		} else if (qName.equals("province")) {
			provinceList.add(province);
		}
	}
	
}