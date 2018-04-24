package com.smartoa.service.impl.screen.beans;

import java.io.InputStreamReader;
import java.net.URL;

import com.alibaba.dubbo.common.utils.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartoa.service.model.screen.other.GeoJson;

public class TestGeoParseMain {
	public static void main(String[] args) throws Exception {
		String content = IOUtils
//				.read(new InputStreamReader(new URL("http://localhost/_test/geo/city.json").openStream()));
//				.read(new InputStreamReader(new URL("http://localhost/_test/geo/province.json").openStream()));
		.read(new InputStreamReader(new URL("http://localhost/_test/geo/district.json").openStream()));
		System.out.println(content);
		ObjectMapper om = new ObjectMapper();
		GeoJson cityJson = om.readValue(content, GeoJson.class);
		System.out.println(cityJson);
	}
}
