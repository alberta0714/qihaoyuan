package com.smartoa.service.impl.screen;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.smartoa.common.constant.Screen;
import com.smartoa.service.ScreenStarter;
import com.smartoa.service.api.IScreenService;
import com.smartoa.service.config.ScreenConfig;
import com.smartoa.service.config.TecScreenRuntimeException;
import com.smartoa.service.impl.screen.holder.ScreenDataHolder;
import com.smartoa.service.impl.screen.quartz.ScreenQuartz;
import com.smartoa.service.impl.screen.utils.HttpUtils;
import com.smartoa.service.impl.screen.utils.ScreenUrlUtils;
import com.smartoa.service.mapper.ScreenMapper;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.screen.map.Areas;
import com.smartoa.service.model.screen.map.Center;
import com.smartoa.service.model.screen.map.ServicerCentral;
import com.smartoa.service.model.screen.other.OperatorAssetsBean;
import com.smartoa.service.model.screen.other.OperatorDataBean;
import com.smartoa.service.model.screen.other.OperatorHeatsBean;
import com.smartoa.service.model.screen.other.ScheduleBean;
import com.smartoa.service.model.screen.other.ScreenServiceArea;
import com.smartoa.service.model.screen.other.ServicerAssetsBean;
import com.smartoa.service.model.screen.other.ServicerDataBean;
import com.smartoa.service.model.screen.other.ServicerHeatsBean;

import lombok.extern.slf4j.Slf4j;

@Service("screenService")
@Slf4j
public class ScreenServiceImpl implements IScreenService {
	@Autowired
	ScreenConfig screenConfig;
	@Autowired
	ScreenMapper screenMapper;

	@Override
	public OperatorDataBean getOperatorDataBean() {
		return ScreenDataHolder.operatorData;
	}

	@Override
	public OperatorAssetsBean getOperatorAssetBean() {
		return ScreenDataHolder.operatorAssets;
	}

	@Override
	public OperatorHeatsBean getOperatorHeatBean() {
		return ScreenDataHolder.operatorHeatsBean;
	}

	@Override
	public Map<String, ServicerDataBean> getServicerDataMap() {
		return ScreenDataHolder.servicerDataMap;
	}

	@Override
	public Map<String, ServicerAssetsBean> getServicerAssetsMap() {
		return ScreenDataHolder.servicerAssetsMap;
	}

	@Override
	public Map<String, ServicerHeatsBean> getServicerHeatsMap() {
		return ScreenDataHolder.servicerHeatsMap;
	}

	@Override
	public Map<String, ScheduleBean> getServicerScheduleMap() {
		return ScreenDataHolder.servicerScheduleMap;
	}

	@Override
	public Map<String, String> getServicerLngLat() {
		return ScreenDataHolder.servicerLngLat;
	}

	@Override
	public Object reload() {
		ScreenQuartz quanzer = ScreenStarter.applicationContext.getBean(ScreenQuartz.class);
		List<Object> msgList = Lists.newArrayList();
		msgList.add("调用成功");
		if (quanzer.isRunning()) {
			msgList.add("其他程序正在更新中, 本调用自动退出.");
			return msgList;
		}
		Stopwatch wt = Stopwatch.createStarted();
		quanzer.scheduler();
		msgList.add("执行完毕");
		msgList.add("用时:" + wt);
		return msgList;
	}

	@Override
	public Object getSub(String servicerId) {
		Map<String, Object> dbBean = null;
		List<ServiceOrg> list = screenMapper.scServiceOrgList(null, "'" + servicerId + "'", null);
		if (CollectionUtils.isEmpty(list)) {
			return "找不到该服务商";
		}
		String cityCode = list.get(0).getAddrCityCode();
		String jsonBody = null;
		dbBean = screenMapper.scQueryVectorData(cityCode);
		if (dbBean == null) {
			try {
				String url = "http://datavmap-public.oss-cn-hangzhou.aliyuncs.com/areas/children/#cityCode#.json"
						.replace("#cityCode#", cityCode);
				jsonBody = new HttpUtils(url).setOriginAliyunDataV().getHtmlText();
				screenMapper.scSaveVectorData(cityCode, jsonBody, url, new Date(), new Date());
				dbBean = screenMapper.scQueryVectorData(cityCode);
				// 存储数据库,缓存
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		return dbBean.get("jsonBody");
	}

	@Override
	public Object getServicerCityCentral(String servicerId) {
		Map<String, Object> dbBean = null;
		List<ServiceOrg> list = screenMapper.scServiceOrgList(null, "'" + servicerId + "'", null);
		if (CollectionUtils.isEmpty(list)) {
			return Lists.newArrayList(new ServicerCentral(null, null, null));
		}
		String cityCode = list.get(0).getAddrCityCode();
		String jsonBody = null;
		dbBean = screenMapper.scQueryCentral(cityCode);
		if (dbBean == null) {
			try {
				String url = "http://datavmap-public.oss-cn-hangzhou.aliyuncs.com/areas/bound/#adcode#.json"
						.replace("#adcode#", cityCode);
				jsonBody = new HttpUtils(url).setOriginAliyunDataV().getHtmlText();
				ObjectMapper om = new ObjectMapper();
				Areas areas = om.readValue(jsonBody, Areas.class);
				Center center = areas.getFeatures().get(0).getProperties().getCenter();
				screenMapper.scSaveCentral(cityCode, center.getLng(), center.getLat(), null, jsonBody, url, new Date(),
						new Date());
				dbBean = screenMapper.scQueryCentral(cityCode);
				// 存储数据库,缓存
			} catch (Exception e) {
				e.printStackTrace();
				return e.getMessage();
			}
		}
		Double lng = (Double) dbBean.get("lng") + (Double) dbBean.get("lngOff");
		Double lat = (Double) dbBean.get("lat") + (Double) dbBean.get("latOff");

		Integer zoom = (Integer) dbBean.get("zoom");
		return Lists.newArrayList(new ServicerCentral(lng, lat, zoom));
	}

	public String buildScreenUrl(String userType, String screenType, String id) throws UnsupportedEncodingException {
		log.debug("userType:{} screenType:{} id:{} ", userType, screenType, id);
		String redirectUrl = "";
		// 运营
		if (userType.equals(Screen.Role.op.name()) && screenType.equals(Screen.Type.data.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenOpDataId(), screenConfig.getScreenOpDataToken(),
					id);
		} else if (userType.equals(Screen.Role.op.name()) && screenType.equals(Screen.Type.assets.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenOpAssetsId(),
					screenConfig.getScreenOpAssetsToken(), id);
		} else if (userType.equals(Screen.Role.op.name()) && screenType.equals(Screen.Type.heats.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenOpHeatsId(),
					screenConfig.getScreenOpHeatsToken(), id);
		} else if (userType.equals(Screen.Role.servicer.name()) && screenType.equals(Screen.Type.data.name())) {
			// 服务商
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenServicerDataId(),
					screenConfig.getScreenServicerDataToken(), id);
		} else if (userType.equals(Screen.Role.servicer.name()) && screenType.equals(Screen.Type.assets.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenServicerAssetsId(),
					screenConfig.getScreenServicerAssetsToken(), id);
		} else if (userType.equals(Screen.Role.servicer.name()) && screenType.equals(Screen.Type.heats.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenServicerHeatsId(),
					screenConfig.getScreenServicerHeatsToken(), id);
		} else if (userType.equals(Screen.Role.servicer.name()) && screenType.equals(Screen.Type.schedule.name())) {
			redirectUrl = ScreenUrlUtils.buildUrl(screenConfig.getScreenServicerScheduleId(),
					screenConfig.getScreenServicerScheduleToken(), id);
		} else {
			throw new TecScreenRuntimeException("存在无法处理的参数");
		}
		return redirectUrl;
	}

	@Override
	public List<ScreenServiceArea> scServiceAreaList() {
		return screenMapper.scServiceAreaList();
	}
}
