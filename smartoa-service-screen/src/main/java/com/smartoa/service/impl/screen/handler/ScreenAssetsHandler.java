package com.smartoa.service.impl.screen.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.smartoa.common.constant.DictionaryEnum;
import com.smartoa.service.ScreenStarter;
import com.smartoa.service.impl.screen.holder.ScreenDataHolder;
import com.smartoa.service.impl.screen.utils.ScreenEnums;
import com.smartoa.service.model.Agreement;
import com.smartoa.service.model.Cust;
import com.smartoa.service.model.Device;
import com.smartoa.service.model.Dictionary;
import com.smartoa.service.model.Engineer;
import com.smartoa.service.model.Order;
import com.smartoa.service.model.OrderSku;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.SysAreaCounty;
import com.smartoa.service.model.SysCity;
import com.smartoa.service.model.SysProvinces;
import com.smartoa.service.model.TsScreenAssets;
import com.smartoa.service.model.screen.components.AreaHeatsLayer;
import com.smartoa.service.model.screen.components.BasicRatar;
import com.smartoa.service.model.screen.components.DigitalFlop;
import com.smartoa.service.model.screen.components.HorizontalBasicBarGraph;
import com.smartoa.service.model.screen.components.Marquee;
import com.smartoa.service.model.screen.components.MultiText;
import com.smartoa.service.model.screen.components.RespiratoryBubble;
import com.smartoa.service.model.screen.components.ShuffingPieChart;
import com.smartoa.service.model.screen.components.StripPieChart;
import com.smartoa.service.model.screen.other.ScreenCust;
import com.smartoa.service.model.screen.other.ScreenDevice;
import com.smartoa.service.model.screen.other.ScreenEngineer;
import com.smartoa.service.model.screen.other.ScreenEngineerDevice;
import com.smartoa.service.model.screen.other.ScreenOrder;
import com.smartoa.service.model.screen.other.ScreenOrderSku;
import com.smartoa.service.model.screen.other.ScreenProductFieldValue;
import com.smartoa.service.model.screen.other.ScreenRegUser;
import com.smartoa.service.model.screen.other.ScreenServiceArea;
import com.smartoa.service.model.screen.other.ScreenServiceOrg;
import com.smartoa.service.model.screen.other.ServicerAssetsBean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScreenAssetsHandler extends ScreenDataHandlerV2 {
	private static final Logger logger = LoggerFactory.getLogger(ScreenAssetsHandler.class);
	Map<String, Object> param = null;

	public void init() {
		Stopwatch wt = Stopwatch.createStarted();
		logger.info(">>>> Screen data initializing...");
		/*
		 * DB加载及缓冲处理
		 */
		// 加载城市区域列表
		Map<String, SysProvinces> provinceMap = mapper.scProvinceList().stream()
				.collect(Collectors.toMap(SysProvinces::getProvincesCode, Function.identity(), (o, n) -> n));
		Map<String, SysCity> cityMap = mapper.scCityList().stream()
				.collect(Collectors.toMap(SysCity::getCityCode, Function.identity(), (o, n) -> n));
		Map<String, SysAreaCounty> areaMap = mapper.scAreaList().stream()
				.collect(Collectors.toMap(SysAreaCounty::getAreaCode, Function.identity(), (o, n) -> n));
		logger.debug("province list size:{} cities:{} areas:{}", provinceMap.size(), cityMap.size(), areaMap.size());
		// 字典
		List<Dictionary> dicList = mapper.scDicList();
		// dicMap type-code-dic
		Map<String, Map<String, Dictionary>> dicMap = buildDicMap(dicList);

		// -- 合同 -订单
		List<Agreement> agreements = mapper.scAgreementList(null, 1, Maps.newHashMap());
		logger.debug("agreements size:{}", agreements.size());
		Map<String, List<Agreement>> agreementOrderIdMap = agreements.stream()
				.collect(Collectors.groupingBy(Agreement::getTbOrderId));
		Set<String> orderIdsSet = agreementOrderIdMap.keySet();
		String orderIds = "'" + StringUtils.join(orderIdsSet, "','") + "'";
		List<Order> orderList = mapper.scOrderList(null, null, orderIds);
		//
		List<ScreenOrder> screenOrderList = initScreenOrder(orderList);
		for (ScreenOrder bean : screenOrderList) {
			try {
				bean.setProvince(provinceMap.get(bean.getOrder().getReceiptAddrProvinceCode()).getProvinces());
				bean.setCity(cityMap.get(bean.getOrder().getReceiptAddrCityCode()).getCity());
				bean.setArea(areaMap.get(bean.getOrder().getReceiptAddrCityCode()).getAreaCounty());
			} catch (Exception e) {
				logger.warn(e.getMessage());
				continue;
			}
		}

		// -- -> 服务商
		List<ScreenServiceOrg> screenServiceOrgList = Lists.newArrayList();
		// Set<String> servicerIdsSet =
		// screenOrderList.stream().collect(Collectors.groupingBy(ScreenOrder::getServicerId))
		// .keySet();
		// String serviceIds = "'" + StringUtils.join(servicerIdsSet, "','") +
		// "'";
		List<ServiceOrg> serviceOrgList = mapper.scServiceOrgList("0,2", null, false); // 企业类型：0-服务商,1-供应商,2-服务供应商
		// create and path GEO
		for (ServiceOrg org : serviceOrgList) {
			ScreenServiceOrg s = new ScreenServiceOrg();
			try {
				s.setProvince(provinceMap.get(org.getAddrProvinceCode()).getProvinces());
				s.setCity(cityMap.get(org.getAddrCityCode()).getCity());
				s.setArea(areaMap.get(org.getAddrCountyCode()).getAreaCounty());
			} catch (Exception e) {
				log.warn("服务商 - 区域信息编码,对应异常", e.getMessage());
			}
			s.setServiceOrg(org);
			s.setServicerId(org.getId());
			screenServiceOrgList.add(s);
		}
		Map<String, ScreenServiceOrg> screenServiceOrgIdMap = screenServiceOrgList.stream()
				.collect(Collectors.toMap(ScreenServiceOrg::getServicerId, Function.identity(), (o, n) -> n));

		// -- -> 有效客户
		List<ScreenCust> screenCustList = Lists.newArrayList();
		Set<String> custIdsSet = screenOrderList.stream().collect(Collectors.groupingBy(ScreenOrder::getCustId))
				.keySet();
		String custIds = "'" + StringUtils.join(custIdsSet, "','") + "'";
		List<Cust> custList = mapper.scCustList(null, null, custIds);
		Map<String, List<ScreenOrder>> custIdToScreenOrderListMap = screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getCustId));
		for (Cust cust : custList) {
			ScreenCust sc = new ScreenCust(cust.getId(), cust);
			screenCustList.add(sc);
			List<ScreenOrder> sList = custIdToScreenOrderListMap.get(cust.getId());
			if (sList == null || sList.isEmpty()) {
				continue;
			}
			sc.setProvince(sList.get(0).getProvince());
			sc.setCity(sList.get(0).getCity());
			sc.setArea(sList.get(0).getArea());
		}
		// patch-servicerId
		Map<String, List<ScreenOrder>> custIdScreenOrderListMap = screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getCustId));
		for (ScreenCust sc : screenCustList) {
			List<ScreenOrder> list = custIdScreenOrderListMap.get(sc.getId());
			if (list == null || list.size() == 0) {
				continue;
			}
			sc.setServicerId(list.get(0).getServicerId());
		}
		// -- -> 加载设备信息 同时统计设备总数
		List<ScreenOrderSku> screenSkuList = Lists.newArrayList();
		List<OrderSku> skuList = mapper.scOrderSkuList(null);
		Map<String, ScreenOrder> screenOrderIdMap = Maps.newHashMap();
		for (ScreenOrder s : screenOrderList) {
			screenOrderIdMap.put(s.getOrder().getId(), s);
		}
		for (OrderSku sku : skuList) {
			if (!orderIds.contains(sku.getOrderNo())) {
				continue;
			}
			ScreenOrderSku oSku = new ScreenOrderSku(sku);
			screenSkuList.add(oSku);
			ScreenOrder so = screenOrderIdMap.get(oSku.getOrderSku().getId());
			if (so == null) {
				continue;
			}
			oSku.setProvince(so.getProvince());
			oSku.setCity(so.getCity());
			oSku.setArea(so.getArea());
		}
		// -- 缓冲 临时表 数据
		int custsCount = custList.size();
		// int devicesInWorkCount = mapper.scDeviceLeaseList(true, null, null).size();
		int devicesInWorkCount = agreements.size();// 合同与设备是一对一的
		int serviceOrgsCount = serviceOrgList.size();
		// -- 加载上报异常-> 设备是否在线 绑定设备
		// List<DeviceReportsAbnormal> deviceReportsAbnormalList =
		// mapper.scDeviceReportsAbnormalList(0);
		// List<ScreenDeviceReportsAbnormal> screenDeviceReportsAbnormalList =
		// buildScreenDeviceReportsAbnormalList(
		// deviceReportsAbnormalList);
		// int onlineCount = deviceReportsAbnormalList.size();
		param = Maps.newHashMap();
		param.put("ifBonded", true);
		List<ScreenEngineerDevice> screenEngineerDeviceList = Lists.newArrayList();
		buildScreenEngineerDeviceList(screenEngineerDeviceList);
		int onlineCount = screenEngineerDeviceList.size();
		try {
			int flag = 0;
			List<TsScreenAssets> assetsToday = mapper.scScreenAssetsList(//
					false, //
					ScreenEnums.ScreenAssetsType.op.name(), null, //
					new java.sql.Date(new java.util.Date().getTime()));
			if (assetsToday.size() == 0) {
				flag = mapper.saveScreenAssets(//
						ScreenEnums.ScreenAssetsType.op.name(), custsCount, devicesInWorkCount, serviceOrgsCount,
						onlineCount, ScreenEnums.ScreenAssetsTypeIdDefault.op_default.name(),
						new java.sql.Date(new java.util.Date().getTime()));
				log.info("save assets compeleted! [affected:{}]", flag);
			} else {
				Integer id = assetsToday.get(0).getId();
				flag = mapper.updateScreenAssets(//
						ScreenEnums.ScreenAssetsType.op.name(), custsCount, devicesInWorkCount, serviceOrgsCount,
						onlineCount, ScreenEnums.ScreenAssetsTypeIdDefault.op_default.name(), id);
				log.info("udpate assets compeleted! [affected:{}]", flag);
			}
			flag = mapper.deleteScreenAssets(new java.sql.Date(new DateTime().minusDays(2).getMillis()));
			logger.info("delete old assets affect count:{}", flag);
		} catch (Exception e) {
			logger.error("保存资产统计数据失败:{}", e.getMessage());
		}

		// -- 设备-> 机型, 黑白, 区域 patch-servicerid
		List<ScreenDevice> screenDeviceList = Lists.newArrayList();
		List<Device> deviceList = mapper.scDeviceList();
		Map<String, Agreement> agreementDeviceIdMap = buildDeviceIdAgreementMap(agreements);
		Set<String> devicesInAgreementSet = agreementDeviceIdMap.keySet();
		for (Device device : deviceList) {
			if (!devicesInAgreementSet.contains(device.getId())) {
				continue;
			}
			ScreenDevice sd = new ScreenDevice(device);
			ScreenOrder so = screenOrderIdMap.get(agreementDeviceIdMap.get(device.getId()).getTbOrderId());
			if (so != null) {
				sd.setProvince(so.getProvince());
				sd.setCity(so.getCity());
				sd.setArea(so.getArea());
				sd.setServicerId(so.getServicerId());
			}
			screenDeviceList.add(sd);
		}
		// path 黑白彩色
		// 根据设备型号，找到对应的色彩
		buildBlackColorProp(screenDeviceList);

		// -- 服务商->区域 , 只加载是否认证

		// -- 工程师表 区域信息, 级别
		List<Engineer> engineerList = mapper.scEngineerList(null, null, null);
		List<ScreenEngineer> screenEngineerList = Lists.newArrayList();
		for (Engineer en : engineerList) {
			ScreenEngineer se = new ScreenEngineer(en);
			try {
				se.setProvince(provinceMap.get(en.getProvinceCode()).getProvinces());
				se.setCity(cityMap.get(en.getCityCode()).getCity());
				se.setArea(areaMap.get(en.getCountyCode()).getAreaCounty());
			} catch (Exception e) {
				log.warn("工程师 - 区域信息编码,对应异常", e.getMessage());
			}
			se.setServicerId(en.getServicerId());
			screenEngineerList.add(se);
			// ScreenEngineer se = new ScreenEngineer(en);
			// ScreenServiceOrg org = screenServiceOrgIdMap.get(en.getServicerId());
			// if (org != null) {
			// se.setProvince(org.getProvince());
			// se.setCity(org.getCity());
			// se.setArea(org.getArea());
			// }
			// se.setServicerId(se.getServicerId());
			// screenEngineerList.add(se);
		}

		/*
		 * XXX 接口数据生成
		 */
		// 运营今天及昨天的翻牌
		buildOpAssetsCount(custsCount, devicesInWorkCount, serviceOrgsCount, onlineCount, onlineCount);
		// servicer init
		Map<String, ServicerAssetsBean> servicerMap = initServicerAssetsBean(screenServiceOrgList);
		// patch servicerOrginfo
		// for (String sId : servicerMap.keySet()) {
		// servicerMap.get(sId).setServicer(
		// screenServiceOrgIdMap.get(sId) == null ? null :
		// screenServiceOrgIdMap.get(sId).getServiceOrg());
		// }
		for (String sId : screenServiceOrgIdMap.keySet()) {
			ServicerAssetsBean bean = servicerMap.get(sId);
			if (bean == null) {
				bean = new ServicerAssetsBean();
				servicerMap.put(sId, bean);
			}
			bean.setServicer(screenServiceOrgIdMap.get(sId).getServiceOrg());
		}
		// servicer 客户数
		buildServicerCustomersCount(screenCustList, servicerMap);
		// servicer 服务设备数
		buildServicerInServiceDevicesCount(screenDeviceList, servicerMap);
		// servicer 在线设备数
		buildServicerOnLineDevicesCount(screenEngineerDeviceList, servicerMap);
		// save
		saveServicerAssetsCount(servicerMap);
		// servicer Yesterday chain ratio
		buildYesterDayChainRatio(servicerMap);

		// -- 每过几分钟-=> 更新大屏数字
		// load yesterday data ,compare and set

		// -- 客户=>行业,区域
		// custList.stream().collect(collector)

		// 机型雷达
		List<BasicRatar> devicesModelRadar = buildDeviceModelRadar(dicMap, screenDeviceList);
		ScreenDataHolder.operatorAssets.setDevicesModelRadar(devicesModelRadar);
		// servicer radar
		Map<String, List<ScreenDevice>> servicerIdScreenDeviceListMap = screenDeviceList.stream()
				.collect(Collectors.groupingBy(ScreenDevice::getServicerId));
		for (String sId : servicerIdScreenDeviceListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			List<ScreenDevice> sdList = servicerIdScreenDeviceListMap.get(sId);
			List<BasicRatar> sdModelRadar = buildDeviceModelRadar(dicMap, sdList);
			asset.setDevicesModelRadar(sdModelRadar);
		}

		// 合约比 就是绑定设备（又称为接入设备） 与 合约设备
		List<StripPieChart> agreementsRatio = Lists.newArrayList();
		agreementsRatio.add(new StripPieChart("有合约", Integer.toString(devicesInWorkCount)));
		agreementsRatio.add(new StripPieChart("无合约", Integer.toString(onlineCount)));
		ScreenDataHolder.operatorAssets.setAgreementsRatio(agreementsRatio);

		// 黑白比
		List<ShuffingPieChart> blackWhiteColorRatio = buildBlackWhiteColorRatio(screenDeviceList);
		ScreenDataHolder.operatorAssets.setBlackWhiteColorRatio(blackWhiteColorRatio);
		// servicer
		for (String sId : servicerIdScreenDeviceListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			List<ScreenDevice> sdList = servicerIdScreenDeviceListMap.get(sId);
			List<ShuffingPieChart> sdModelRadar = buildBlackWhiteColorRatio(sdList);
			asset.setBlackWhiteColorRatio(sdModelRadar);
		}

		// 工程师比
		List<ShuffingPieChart> engineerLevelRatio = buildEngineerLevelRatio(screenEngineerList);
		ScreenDataHolder.operatorAssets.setEngineerLevelRatio(engineerLevelRatio);
		// servicer radar
		Map<String, List<ScreenEngineer>> servicerIdScreenEngineerListMap = screenEngineerList.stream()
				.filter(item -> StringUtils.isNotEmpty(item.getServicerId()))
				.collect(Collectors.groupingBy(ScreenEngineer::getServicerId));
		for (String sId : servicerIdScreenEngineerListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			asset.setEngineerLevelRatio(buildEngineerLevelRatio(servicerIdScreenEngineerListMap.get(sId)));
		}

		// 服务设备 城市排名 && 设备 气泡
		buildInServiceDevicesAndDeviceBubbles(screenDeviceList);
		buildServicerInServiceDevicesAndDeviceBubbles(servicerMap, servicerIdScreenDeviceListMap);

		// 认证服务商 柱状图 && 服务商气泡
		List<HorizontalBasicBarGraph> certifationServicersRank = Lists.newArrayList();
		List<RespiratoryBubble> bubbleServicers = Lists.newArrayList();
		screenServiceOrgList.stream().collect(Collectors.groupingBy(ScreenServiceOrg::getCity))
				.forEach((city, list) -> {
					if (list.isEmpty()) {
						return; // continue
					}
					if (StringUtils.isEmpty(city)) {
						city = "未知";
					}
					certifationServicersRank.add(new HorizontalBasicBarGraph(city, Integer.toString(list.size()), "1"));
					bubbleServicers.add(new RespiratoryBubble(list.get(0).getServiceOrg().getLng(),
							list.get(0).getServiceOrg().getLat(), (double) list.size(), 1d));
				});
		Collections.sort(certifationServicersRank,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		ScreenDataHolder.operatorAssets.setCertifationServicersRank(certifationServicersRank);
		ScreenDataHolder.operatorAssets.setBubbleServicers(bubbleServicers);

		// 客户 城市排名 客户 气泡
		buildCustomersRankAndBubbles(screenCustList);
		// servicer customer's rank and bubbles
		Map<String, List<ScreenCust>> servicerIdScreenCustListMap = screenCustList.stream()
				.collect(Collectors.groupingBy(ScreenCust::getServicerId));
		buildServicerCustomersRankAndBubbles(servicerMap, servicerIdScreenCustListMap);

		// 工程师 - 城市排名
		buildEngineerRank(screenEngineerList);
		// servicer's engineer rank
		buildServicerEngineerRank(servicerIdScreenEngineerListMap, servicerMap);

		// 注册会员数统计
		List<ScreenRegUser> regUsersList = mapper.scRegUsers();
		ScreenDataHolder.operatorAssets
				.setRegUsersCount(Lists.newArrayList(new Marquee("当前注册会员数：" + regUsersList.size() + "个")));

		// 绑定接入设备排名
		Map<String, List<ScreenEngineerDevice>> cityBondedRank = Maps.newHashMap();
		for (ScreenEngineerDevice sed : screenEngineerDeviceList) {
			List<ScreenEngineerDevice> cityList = cityBondedRank.get(sed.getCity());
			if (cityList == null) {
				cityList = Lists.newArrayList();
				cityBondedRank.put(sed.getCity(), cityList);
			}
			cityList.add(sed);
		}
		List<HorizontalBasicBarGraph> bonedDeviceRank = Lists.newArrayList();
		for (String city : cityBondedRank.keySet()) {
			bonedDeviceRank.add(new HorizontalBasicBarGraph(city == null ? "北京市" : city,
					Integer.toString(cityBondedRank.get(city).size()), "1"));
		}
		ScreenDataHolder.operatorAssets.setBonedDeviceRank(bonedDeviceRank);
		// TODO 服务商，接入绑定设备区域排名

		// 授权区域的高亮
		List<ScreenServiceArea> authAreas = mapper.scServiceAreaList();
		buildAuthAreas(authAreas);
		buildServicerAuthAreas(authAreas, servicerMap);
		ScreenDataHolder.servicerAssetsMap = servicerMap;
		logger.info("<<<< Initializing screen data  compeleted! {}", wt);
	}
	private void buildAuthAreas(List<ScreenServiceArea> authAreas) {
		Stopwatch wt = Stopwatch.createStarted();
		List<AreaHeatsLayer> lst =Lists.newArrayList();
		for (ScreenServiceArea info : authAreas) {
			lst.add(new AreaHeatsLayer(info.getProvinceCode(), 0.5d));
		}
		ScreenDataHolder.operatorAssets.setAuthAreas(lst);
		logger.info("- 运营的高亮授权区域计算完毕.{}", wt);
	}

	private void buildServicerAuthAreas(List<ScreenServiceArea> authAreas, Map<String, ServicerAssetsBean> servicerMap) {
		Stopwatch wt = Stopwatch.createStarted();
		// build map
		Map<String, List<AreaHeatsLayer>> authMap = Maps.newHashMap();
		for (ScreenServiceArea a : authAreas) {
			List<AreaHeatsLayer> lst = authMap.get(a.getSId());
			if (lst == null) {
				lst = Lists.newArrayList();
				authMap.put(a.getSId(), lst);
			}
			// XXX 暂时定为0.5 今后,希望不仅可以高亮,并且能显示出来更多的意义
			lst.add(new AreaHeatsLayer(a.getCountryCode(), 0.5d));
		}
		// set back
		for (String sId : servicerMap.keySet()) {
			servicerMap.get(sId).setAuthAreas(authMap.get(sId));
		}
		logger.info("- 服务商的高亮授权区域计算完毕.{}", wt);
	}

	private void buildBlackColorProp(List<ScreenDevice> screenDeviceList) {
		List<ScreenProductFieldValue> screenProductFieldValueList = mapper.scProductFieldValue("color");
		Map<String, ScreenProductFieldValue> screenProductFieldValueModelMap = screenProductFieldValueList.stream()
				.collect(Collectors.toMap(ScreenProductFieldValue::getModel, Function.identity(), (o, n) -> n));
		for (ScreenDevice d : screenDeviceList) {
			ScreenProductFieldValue fd = screenProductFieldValueModelMap.get(d.getDevice().getDeviceModelDictCode());
			if (fd == null) {
				continue;
			}
			d.setColor(fd.getFieldValue());
		}
	}

	private void buildScreenEngineerDeviceList(List<ScreenEngineerDevice> screenEngineerDeviceList) {
		List<Map<String, Object>> engineerDeviceList = mapper.scEngineerDevice(param);
		for (Map<String, Object> rs : engineerDeviceList) {
			ScreenEngineerDevice ed = new ScreenEngineerDevice();
			String id = rs.get("id") == null ? null : rs.get("id").toString();
			String tbEngineerId = rs.get("tbEngineerId") == null ? null : rs.get("tbEngineerId").toString();
			String tbDeviceId = rs.get("tbDeviceId") == null ? null : rs.get("tbDeviceId").toString();
			String servicerId = rs.get("servicerId") == null ? null : rs.get("servicerId").toString();
			if (StringUtils.isEmpty(tbDeviceId) || StringUtils.isEmpty(tbEngineerId)
					|| StringUtils.isEmpty(servicerId)) {
				logger.debug("工程师设备表数据，设备ID工程师ID服务商ID，不可为空。此数据ID为：{}", id);
				continue;
			}
			ed.setId(id);
			ed.setTbEngineerId(tbEngineerId);
			ed.setTbDeviceId(tbDeviceId);
			ed.setServicerId(servicerId);
			screenEngineerDeviceList.add(ed);
		}
	}

	void buildServicerEngineerRank(Map<String, List<ScreenEngineer>> servicerIdScreenEngineerListMap,
			Map<String, ServicerAssetsBean> servicerMap) {
		for (String sId : servicerIdScreenEngineerListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			List<ScreenEngineer> seList = servicerIdScreenEngineerListMap.get(sId);
			List<HorizontalBasicBarGraph> engineersRank = Lists.newArrayList();
			seList.stream().collect(Collectors.groupingBy(ScreenEngineer::getArea)).forEach((area, list) -> {
				if (StringUtils.isEmpty(area)) {
					area = "未知";
				}
				engineersRank.add(new HorizontalBasicBarGraph(area, Integer.toString(list.size()), "1"));
			});
			Collections.sort(engineersRank,
					Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
			asset.setEngineersRank(engineersRank);
		}
	}

	private void buildEngineerRank(List<ScreenEngineer> screenEngineerList) {
		List<HorizontalBasicBarGraph> engineersRank = Lists.newArrayList();
		screenEngineerList.stream().collect(Collectors.groupingBy(ScreenEngineer::getCity)).forEach((city, list) -> {
			if (StringUtils.isEmpty(city)) {
				city = "未知";
			}
			engineersRank.add(new HorizontalBasicBarGraph(city, Integer.toString(list.size()), "1"));
		});
		Collections.sort(engineersRank, Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		ScreenDataHolder.operatorAssets.setEngineersRank(engineersRank);
	}

	private void buildServicerCustomersRankAndBubbles(Map<String, ServicerAssetsBean> servicerMap,
			Map<String, List<ScreenCust>> servicerIdScreenCustListMap) {
		for (String sId : servicerIdScreenCustListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			List<ScreenCust> scList = servicerIdScreenCustListMap.get(sId);

			List<HorizontalBasicBarGraph> customersRank = Lists.newArrayList();
			List<RespiratoryBubble> bubbleCustomers = Lists.newArrayList();
			scList.stream().collect(Collectors.groupingBy(ScreenCust::getArea)).forEach((area, list) -> {
				if (StringUtils.isEmpty(area)) {
					area = "未知";
				}
				customersRank.add(new HorizontalBasicBarGraph(area, Integer.toString(list.size()), "1"));
				if (list.isEmpty()) {
					return;
				}
				String lng = list.get(0).getCust().getLng();
				String lat = list.get(0).getCust().getLat();
				if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
					// 116.407851,39.91408
					lng = "116.407851";
					lat = "39.91408";
				}
				if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
					lng = Double.toString(NumberUtils.toDouble(lng));
					lat = Double.toString(NumberUtils.toDouble(lat));
				}
				bubbleCustomers.add(new RespiratoryBubble(lng, lat, (double) list.size(), 1d));
			});
			Collections.sort(customersRank,
					Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
			asset.setCustomersRank(customersRank);
			asset.setBubbleCustomers(bubbleCustomers);
		}
	}

	private void buildCustomersRankAndBubbles(List<ScreenCust> screenCustList) {
		List<HorizontalBasicBarGraph> customersRank = Lists.newArrayList();
		List<RespiratoryBubble> bubbleCustomers = Lists.newArrayList();
		screenCustList.stream().collect(Collectors.groupingBy(ScreenCust::getCity)).forEach((city, list) -> {
			customersRank.add(new HorizontalBasicBarGraph(city, Integer.toString(list.size()), "1"));
			if (list.isEmpty()) {
				return;
			}
			String lng = list.get(0).getCust().getLng();
			String lat = list.get(0).getCust().getLat();
			if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
				// 116.407851,39.91408
				lng = "116.407851";
				lat = "39.91408";
			}
			if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
				lng = Double.toString(NumberUtils.toDouble(lng) - 0.3);
				lat = Double.toString(NumberUtils.toDouble(lat) - 0.3);
			}
			bubbleCustomers.add(new RespiratoryBubble(lng, lat, (double) list.size(), 1d));
		});
		Collections.sort(customersRank, Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		ScreenDataHolder.operatorAssets.setCustomersRank(customersRank);
		ScreenDataHolder.operatorAssets.setBubbleCustomers(bubbleCustomers);
	}

	private void buildServicerInServiceDevicesAndDeviceBubbles(Map<String, ServicerAssetsBean> servicerMap,
			Map<String, List<ScreenDevice>> servicerIdScreenDeviceListMap) {
		for (String sId : servicerIdScreenDeviceListMap.keySet()) {
			ServicerAssetsBean asset = servicerMap.get(sId);
			if (asset == null) {
				continue;
			}
			List<ScreenDevice> sdList = servicerIdScreenDeviceListMap.get(sId);

			List<HorizontalBasicBarGraph> inServiceDevicesServicerRank = Lists.newArrayList();
			List<RespiratoryBubble> bubbleServicerDevices = Lists.newArrayList();
			sdList.stream().collect(Collectors.groupingBy(ScreenDevice::getArea)).forEach((area, devList) -> {
				if (StringUtils.isEmpty(area)) {
					area = "未知";
				}
				inServiceDevicesServicerRank
						.add(new HorizontalBasicBarGraph(area, Integer.toString(devList.size()), "1"));
				if (devList.isEmpty()) {
					return;// same with continue
				}
				String lng = devList.get(0).getDevice().getLng();
				String lat = devList.get(0).getDevice().getLat();
				if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
					// 116.407851,39.91408
					lng = "116.407851";
					lat = "39.91408";
				}
				if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
					lng = Double.toString(NumberUtils.toDouble(lng) + 0.01d);
					lat = Double.toString(NumberUtils.toDouble(lat) + 0.01d);
				}
				bubbleServicerDevices.add(new RespiratoryBubble(lng, lat, (double) devList.size(), 1d));
			});
			Collections.sort(inServiceDevicesServicerRank,
					Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));

			asset.setInServiceDevicesRank(inServiceDevicesServicerRank);
			asset.setBubbleDevices(bubbleServicerDevices);
		}
	}

	private void buildInServiceDevicesAndDeviceBubbles(List<ScreenDevice> screenDeviceList) {
		List<HorizontalBasicBarGraph> inServiceDevicesRank = Lists.newArrayList();
		List<RespiratoryBubble> bubbleDevices = Lists.newArrayList();
		screenDeviceList.stream().collect(Collectors.groupingBy(ScreenDevice::getCity)).forEach((city, devList) -> {
			inServiceDevicesRank.add(new HorizontalBasicBarGraph(city, Integer.toString(devList.size()), "1"));
			if (devList.isEmpty()) {
				return;// same with continue
			}
			String lng = devList.get(0).getDevice().getLng();
			String lat = devList.get(0).getDevice().getLat();
			if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
				// 116.407851,39.91408
				lng = "116.407851";
				lat = "39.91408";
			}
			if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
				lng = Double.toString(NumberUtils.toDouble(lng) + 0.3d);
				lat = Double.toString(NumberUtils.toDouble(lat) + 0.3d);
			}
			// 默认为北京
			bubbleDevices.add(new RespiratoryBubble(lng, lat, (double) devList.size(), 1d));
		});
		Collections.sort(inServiceDevicesRank,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		ScreenDataHolder.operatorAssets.setInServiceDevicesRank(inServiceDevicesRank);
		ScreenDataHolder.operatorAssets.setBubbleDevices(bubbleDevices);
	}

	private List<ShuffingPieChart> buildEngineerLevelRatio(List<ScreenEngineer> screenEngineerList) {
		List<ShuffingPieChart> engineerLevelRatio = Lists.newArrayList();
		Map<String, List<ScreenEngineer>> engineerLevelMap = Maps.newHashMap();
		for (ScreenEngineer se : screenEngineerList) {
			if (se.getEngineer().getIfAuthentication() == null || se.getEngineer().getIfAuthentication() != 1) {
				continue;
			}
			Short level = se.getEngineer().getLevel();
			String levelKey = level == null ? "" : Short.toString(level);
			List<ScreenEngineer> list = engineerLevelMap.get(levelKey);
			if (list == null) {
				list = Lists.newArrayList();
				engineerLevelMap.put(levelKey, list);
			}
			list.add(se);
		}
		for (String level : engineerLevelMap.keySet()) {
			String levelName = level;
			if (level.equals("0")) {
				levelName = "初级";
			} else if (level.equals("1")) {
				levelName = "中级";
			} else if (level.equals("2")) {
				levelName = "高级";
			}
			engineerLevelRatio.add(new ShuffingPieChart(levelName, engineerLevelMap.get(level).size()));
		}
		return engineerLevelRatio;
	}

	private List<ShuffingPieChart> buildBlackWhiteColorRatio(List<ScreenDevice> screenDeviceList) {
		List<ShuffingPieChart> blackWhiteColorRatio = Lists.newArrayList();
		int colorCount = 0;
		int blackCount = 0;
		for (ScreenDevice sd : screenDeviceList) {
			if (StringUtils.isEmpty(sd.getColor())) {
				continue;
			}
			if (sd.getColor().equals("color")) {
				colorCount += 1;
			} else if (sd.getColor().equals("black_white")) {
				blackCount += 1;
			}
		}
		blackWhiteColorRatio.add(new ShuffingPieChart("彩色机", colorCount));
		blackWhiteColorRatio.add(new ShuffingPieChart("黑白机", blackCount));
		return blackWhiteColorRatio;
	}

	private List<BasicRatar> buildDeviceModelRadar(Map<String, Map<String, Dictionary>> dicMap,
			List<ScreenDevice> screenDeviceList) {
		List<BasicRatar> devicesModelRadar = Lists.newArrayList();
		Map<String, List<ScreenDevice>> deviceModelMap = Maps.newHashMap();
		for (ScreenDevice sd : screenDeviceList) {
			Map<String, Dictionary> dicItemMap = dicMap.get(DictionaryEnum.DEVICE_MODEL.value());
			String modelName = "";
			if (dicItemMap != null //
					&& dicItemMap.get(sd.getDevice().getDeviceModelDictCode()) != null) {
				modelName = dicItemMap.get(sd.getDevice().getDeviceModelDictCode()).getName();
			}
			List<ScreenDevice> list = deviceModelMap.get(modelName);
			if (list == null) {
				list = Lists.newArrayList();
				deviceModelMap.put(modelName, list);
			}
			list.add(sd);
		}
		for (String model : deviceModelMap.keySet()) {
			devicesModelRadar.add(new BasicRatar(model, Integer.toString(deviceModelMap.get(model).size()), "1"));
		}
		return devicesModelRadar;
	}

	private void buildYesterDayChainRatio(Map<String, ServicerAssetsBean> servicerMap) {
		List<TsScreenAssets> assetsServicerYesterdayList = mapper.scScreenAssetsList(//
				true, //
				ScreenEnums.ScreenAssetsType.servicer.name(), null, //
				new java.sql.Date(new DateTime().minusDays(1).getMillis()));
		Map<String, List<TsScreenAssets>> servicerIdAssetsListMap = assetsServicerYesterdayList.stream()
				.collect(Collectors.groupingBy(TsScreenAssets::getTypeId));
		for (String sId : servicerMap.keySet()) {
			ServicerAssetsBean assets = servicerMap.get(sId);
			List<TsScreenAssets> yList = servicerIdAssetsListMap.get(sId);
			if (yList == null || yList.size() == 0) {
				// init
				assets.setCustomersCountYesterday(Lists.newArrayList(new MultiText("昨日:-家,环比:-%")));
				assets.setInServiceDevicesCountYesterday(Lists.newArrayList(new MultiText("昨日:-台,环比:-%")));
				assets.setOnlineDevicesCountYesterday(Lists.newArrayList(new MultiText("昨日:-台,环比:-%")));
				continue;
			}
			TsScreenAssets yAssets = yList.get(0);
			assets.setCustomersCountYesterday(
					Lists.newArrayList(new MultiText("昨日:" + yAssets.getCustomers() + "家,环比:" + chainRatio(//
							assets.getCustomersCount() == null ? 0 : assets.getCustomersCount().get(0).getValue()//
							, yAssets.getCustomers()))));
			assets.setInServiceDevicesCountYesterday(
					Lists.newArrayList(new MultiText("昨日:" + yAssets.getServiceDevices() + "台,环比:"//
							+ chainRatio(
									assets.getInServiceDevicesCount() == null ? 0
											: assets.getInServiceDevicesCount().get(0).getValue(), //
									yAssets.getServiceDevices())//
					)));
			assets.setOnlineDevicesCountYesterday(Lists.newArrayList(new MultiText("昨日:" + yAssets.getOnlineDevices()
					+ "台,环比:" + chainRatio(//
							assets.getOnlineDevicesCount() == null ? 0
									: assets.getOnlineDevicesCount().get(0).getValue()//
							, yAssets.getOnlineDevices()//
					))));
		}
	}

	private void buildOpAssetsCount(int custsCount, int devicesInWorkCount, int serviceOrgsCount, int onLineDeviceCount,
			int onlineCount) {
		// 四个翻牌
		ScreenDataHolder.operatorAssets.setCustomersCount(Lists.newArrayList(new DigitalFlop("合同客户数", custsCount)));
		ScreenDataHolder.operatorAssets
				.setInServiceDevicesCount(Lists.newArrayList(new DigitalFlop("合同设备台数", devicesInWorkCount)));
		ScreenDataHolder.operatorAssets
				.setCertifationServicersCount(Lists.newArrayList(new DigitalFlop("认证服务商数", serviceOrgsCount)));
		ScreenDataHolder.operatorAssets
				.setOnlineDevicesCount(Lists.newArrayList(new DigitalFlop("绑定设备台数", onlineCount)));
		// 四个环比
		// save result and load yesterday data 仅保近留两天的数据
		List<TsScreenAssets> assetsOpYesterdayList = mapper.scScreenAssetsList(//
				true, //
				ScreenEnums.ScreenAssetsType.op.name(), null, //
				new java.sql.Date(new DateTime().minusDays(1).getMillis()));
		if (assetsOpYesterdayList != null && !assetsOpYesterdayList.isEmpty()) {
			TsScreenAssets tsScreenAssetsYesterday = assetsOpYesterdayList.get(0);

			ScreenDataHolder.operatorAssets.setCustomersCountYesterday(Lists.newArrayList(//
					new MultiText("昨日:" + tsScreenAssetsYesterday.getCustomers() + "家 环比: "//
							+ chainRatio(custsCount, tsScreenAssetsYesterday.getCustomers()))));
			ScreenDataHolder.operatorAssets.setInServiceDevicesCountYesterday(
					Lists.newArrayList(new MultiText("昨日:" + tsScreenAssetsYesterday.getServiceDevices() + "台 环比: " //
							+ chainRatio(devicesInWorkCount, tsScreenAssetsYesterday.getServiceDevices()))));

			ScreenDataHolder.operatorAssets.setCertificationServicersCountYesterday(Lists
					.newArrayList(new MultiText("昨日:" + tsScreenAssetsYesterday.getCertificationServicers() + "家 环比: "//
							+ chainRatio(serviceOrgsCount, tsScreenAssetsYesterday.getCertificationServicers()))));
			ScreenDataHolder.operatorAssets.setOnlineDevicesCountYesterday(
					Lists.newArrayList(new MultiText("昨日:" + tsScreenAssetsYesterday.getOnlineDevices() + "台 环比: " //
							+ chainRatio(onLineDeviceCount, tsScreenAssetsYesterday.getOnlineDevices()))));
		} else {
			ScreenDataHolder.operatorAssets.setCustomersCountYesterday(Lists.newArrayList(new MultiText("昨日:-家 环比:-")));
			ScreenDataHolder.operatorAssets
					.setInServiceDevicesCountYesterday(Lists.newArrayList(new MultiText("昨日:-台 环比:- ")));
			ScreenDataHolder.operatorAssets
					.setCertificationServicersCountYesterday(Lists.newArrayList(new MultiText("昨日:-家 环比: -")));
			ScreenDataHolder.operatorAssets
					.setOnlineDevicesCountYesterday(Lists.newArrayList(new MultiText("昨日:-台 环比: -")));
		}
	}

	private Map<String, ServicerAssetsBean> initServicerAssetsBean(List<ScreenServiceOrg> screenServiceOrgList) {
		Map<String, ServicerAssetsBean> servicerMap = Maps.newHashMap();
		for (ScreenServiceOrg bean : screenServiceOrgList) {
			servicerMap.put(bean.getServicerId(), new ServicerAssetsBean());
		}
		return servicerMap;
	}

	private void buildServicerCustomersCount(List<ScreenCust> screenCustList,
			Map<String, ServicerAssetsBean> servicerMap) {
		Map<String, List<ScreenCust>> sIdScreenCustListMap = screenCustList.stream()
				.collect(Collectors.groupingBy(ScreenCust::getServicerId));
		for (String sId : sIdScreenCustListMap.keySet()) {
			ServicerAssetsBean data = servicerMap.get(sId);
			if (data == null) {
				data = new ServicerAssetsBean();
				servicerMap.put(sId, data);
			}
			List<ScreenCust> list = sIdScreenCustListMap.get(sId);
			data.setCustomersCount(Lists.newArrayList(new DigitalFlop("客户数", list == null ? 0 : list.size())));
		}
	}

	private void buildServicerInServiceDevicesCount(List<ScreenDevice> screenDeviceList,
			Map<String, ServicerAssetsBean> servicerMap) {
		Map<String, List<ScreenDevice>> sIdScreenDeviceListMap = screenDeviceList.stream()
				.collect(Collectors.groupingBy(ScreenDevice::getServicerId));
		for (String sId : sIdScreenDeviceListMap.keySet()) {
			ServicerAssetsBean data = servicerMap.get(sId);
			if (data == null) {
				data = new ServicerAssetsBean();
				servicerMap.put(sId, data);
			}
			List<ScreenDevice> list = sIdScreenDeviceListMap.get(sId);
			data.setInServiceDevicesCount(Lists.newArrayList(new DigitalFlop("服务设备数", list == null ? 0 : list.size())));
		}
	}

	private void buildServicerOnLineDevicesCount(List<ScreenEngineerDevice> screenEngineerDeviceList,
			Map<String, ServicerAssetsBean> servicerMap) {
		Map<String, List<ScreenEngineerDevice>> sIdScreenDeviceReportsAbnormalListMap = screenEngineerDeviceList
				.stream().collect(Collectors.groupingBy(ScreenEngineerDevice::getServicerId));
		for (String sId : sIdScreenDeviceReportsAbnormalListMap.keySet()) {
			ServicerAssetsBean data = servicerMap.get(sId);
			if (data == null) {
				data = new ServicerAssetsBean();
				servicerMap.put(sId, data);
			}
			List<ScreenEngineerDevice> list = sIdScreenDeviceReportsAbnormalListMap.get(sId);
			data.setOnlineDevicesCount(Lists.newArrayList(new DigitalFlop("绑定设备台数", list == null ? 0 : list.size())));
		}
	}

	private void saveServicerAssetsCount(Map<String, ServicerAssetsBean> servicerMap) {
		for (String sId : servicerMap.keySet()) {
			ServicerAssetsBean bean = servicerMap.get(sId);

			int customersCount = 0;
			int inServiceDevicesCount = 0;
			int onlineDevicesCount = 0;

			if (!CollectionUtils.isEmpty(bean.getCustomersCount())) {
				customersCount = (int) bean.getCustomersCount().get(0).getValue();
			}
			if (!CollectionUtils.isEmpty(bean.getInServiceDevicesCount())) {
				inServiceDevicesCount = (int) bean.getInServiceDevicesCount().get(0).getValue();
			}
			if (!CollectionUtils.isEmpty(bean.getOnlineDevicesCount())) {
				onlineDevicesCount = (int) bean.getOnlineDevicesCount().get(0).getValue();
			}
			// try {
			// mapper.saveScreenAssets(//
			// ScreenEnums.ScreenAssetsType.servicer.name()//
			// , customersCount, inServiceDevicesCount, 1, onlineDevicesCount//
			// , sId, new Date());
			// } catch (Exception e) {
			// logger.warn(e.getMessage());
			// }
			try {
				int flag = 0;
				List<TsScreenAssets> assetsToday = mapper.scScreenAssetsList(//
						false, //
						ScreenEnums.ScreenAssetsType.servicer.name(), sId, //
						new java.sql.Date(new DateTime().getMillis()));
				if (assetsToday.size() == 0) {
					flag = mapper.saveScreenAssets(//
							ScreenEnums.ScreenAssetsType.servicer.name()//
							, customersCount, inServiceDevicesCount, 1, onlineDevicesCount//
							, sId, new Date());
					log.info("save assets compeleted! [affected:{}]", flag);
				} else {
					Integer id = assetsToday.get(0).getId();
					flag = mapper.updateScreenAssets(//
							ScreenEnums.ScreenAssetsType.servicer.name()//
							, customersCount, inServiceDevicesCount, 1, onlineDevicesCount//
							, sId, id);
					log.info("udpate assets compeleted! [affected:{}]", flag);
				}
			} catch (Exception e) {
				logger.error("保存资产统计数据失败:{}", e.getMessage());
			}
		}
	}

	private Map<String, Agreement> buildDeviceIdAgreementMap(List<Agreement> agreements) {
		Map<String, Agreement> agreementDeviceIdMap = Maps.newHashMap();
		for (Agreement bean : agreements) {
			if (StringUtils.isEmpty(bean.getTbDeviceId())) {
				continue;
			}
			agreementDeviceIdMap.put(bean.getTbDeviceId(), bean);
		}
		agreementDeviceIdMap.remove(null);
		return agreementDeviceIdMap;
	}

	/**
	 * 环比
	 * 
	 * @param a
	 *            今天
	 * @param b
	 *            昨天
	 */
	public String chainRatio(double a, double b) {
		if (b == 0 || a == 0) { // 若没有可比性,则直接 返回 0
			return "0.00%";
		}
		// if (b == 0 && a != 0) { // return ${a}*100%
		// b = 1;
		// a = a + 1;
		// }
		double ratio = (a - b) / b * 100d;// %
		BigDecimal decimal = new BigDecimal(ratio);
		decimal = decimal.setScale(2, RoundingMode.HALF_UP);
		String r = Double.toString(decimal.doubleValue());
		return r + "%";
	}

	public static void main(String[] args) throws UnknownHostException {
		ScreenStarter.main(args);
		new ScreenAssetsHandler().init();
	}
}
