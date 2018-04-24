package com.smartoa.service.impl.screen.holder;

import java.io.Serializable;
import java.util.Map;

import com.smartoa.service.model.screen.other.OperatorAssetsBean;
import com.smartoa.service.model.screen.other.OperatorDataBean;
import com.smartoa.service.model.screen.other.OperatorHeatsBean;
import com.smartoa.service.model.screen.other.ScheduleBean;
import com.smartoa.service.model.screen.other.ServicerAssetsBean;
import com.smartoa.service.model.screen.other.ServicerDataBean;
import com.smartoa.service.model.screen.other.ServicerHeatsBean;

import jersey.repackaged.com.google.common.collect.Maps;
import lombok.Data;

@Data
public class ScreenDataHolder implements Serializable {
	private static final long serialVersionUID = 372143324962797479L;
	// 运营大屏
	public static final OperatorDataBean operatorData = new OperatorDataBean();
	public static final OperatorAssetsBean operatorAssets = new OperatorAssetsBean();
	public static final OperatorHeatsBean operatorHeatsBean = new OperatorHeatsBean();
	// 服务大屏
	public static Map<String, ServicerDataBean> servicerDataMap = Maps.newHashMap();
	public static Map<String, ServicerAssetsBean> servicerAssetsMap = Maps.newHashMap();
	public static Map<String, ServicerHeatsBean> servicerHeatsMap = Maps.newHashMap();
	public static Map<String, ScheduleBean> servicerScheduleMap = Maps.newHashMap();
	// 统一存储 服务商的经纬度
	public static Map<String, String> servicerLngLat = Maps.newHashMap();
}