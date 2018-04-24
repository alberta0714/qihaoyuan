package com.smartoa.service.impl.screen.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.smartoa.common.constant.DictionaryEnum;
import com.smartoa.service.ScreenStarter;
import com.smartoa.service.impl.screen.holder.ScreenDataHolder;
import com.smartoa.service.impl.screen.utils.NumberFormat;
import com.smartoa.service.mapper.ScreenMapper;
import com.smartoa.service.model.Agreement;
import com.smartoa.service.model.Cust;
import com.smartoa.service.model.Dictionary;
import com.smartoa.service.model.Engineer;
import com.smartoa.service.model.Order;
import com.smartoa.service.model.OrderSku;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.SysAreaCounty;
import com.smartoa.service.model.SysCity;
import com.smartoa.service.model.SysProvinces;
import com.smartoa.service.model.WorkOrder;
import com.smartoa.service.model.screen.components.DigitalFlop;
import com.smartoa.service.model.screen.components.FlyLine;
import com.smartoa.service.model.screen.components.HorizontalBasicBarGraph;
import com.smartoa.service.model.screen.components.HorizontalCapsuleBarGraph;
import com.smartoa.service.model.screen.components.LabelComparisonPieChart;
import com.smartoa.service.model.screen.components.MultiText;
import com.smartoa.service.model.screen.components.RespiratoryBubble;
import com.smartoa.service.model.screen.components.ShuffingListHotProduct;
import com.smartoa.service.model.screen.components.ShuffingPieChart;
import com.smartoa.service.model.screen.components.StripPieChart;
import com.smartoa.service.model.screen.other.ScheduleBean;
import com.smartoa.service.model.screen.other.ScreenCust;
import com.smartoa.service.model.screen.other.ScreenMember;
import com.smartoa.service.model.screen.other.ScreenOrder;
import com.smartoa.service.model.screen.other.ScreenWorkOrder;
import com.smartoa.service.model.screen.other.ScreenWorkOrderOpr;
import com.smartoa.service.model.screen.other.ServicerDataBean;
import com.smartoa.service.model.screen.other.SumBean;

import jersey.repackaged.com.google.common.collect.Lists;

public class ScreenDataHandlerV2 {
	static final Logger logger = LoggerFactory.getLogger(ScreenDataHandlerV2.class);
	ScreenMapper mapper = (ScreenMapper) ScreenStarter.applicationContext.getBean("screenMapper");
	DateTime dt = new DateTime();
	DateTime dtStart = dt.minusMonths(3);
	DateTime dtEnd = new DateTime(dt);

	public void init() {
		logger.info(">>>> Screen data initializing...");
		Stopwatch wt = Stopwatch.createStarted();
		/*
		 * DAO query加载数据
		 */
		// 订单列表
		String start = dtStart.toString(DatePattern.YYYYMMDDHHMMSS.val());
		String end = dtEnd.toString(DatePattern.YYYYMMDDHHMMSS.val());
		List<Order> orderList = mapper.scOrderList(start, end, null);
		List<ScreenOrder> screenOrderList = initScreenOrder(orderList);
		// 加载城市区域列表
		Map<String, SysProvinces> provinceMap = mapper.scProvinceList().stream()
				.collect(Collectors.toMap(SysProvinces::getProvincesCode, Function.identity(), (o, n) -> n));
		Map<String, SysCity> cityMap = mapper.scCityList().stream()
				.collect(Collectors.toMap(SysCity::getCityCode, Function.identity(), (o, n) -> n));
		Map<String, SysAreaCounty> areaMap = mapper.scAreaList().stream()
				.collect(Collectors.toMap(SysAreaCounty::getAreaCode, Function.identity(), (o, n) -> n));
		logger.debug("province list size:{} cities:{} areas:{}", provinceMap.size(), cityMap.size(), areaMap.size());
		List<Dictionary> dicList = mapper.scDicList();
		// dicMap type-code-dic
		Map<String, Map<String, Dictionary>> dicMap = buildDicMap(dicList);
		logger.debug("dic map build compeleted! {}", dicMap.size());

		// 合同,订单单品, 客户列表
		List<Agreement> agreementList = mapper.scAgreementList(buildOrderIds(orderList), null, Maps.newHashMap());
		List<OrderSku> orderSkuList = mapper.scOrderSkuList(buildOrderNos(orderList));

		List<ScreenCust> screenCustList = Lists.newArrayList();
		List<Cust> custList = buildScreenCustList(mapper, start, end, screenCustList);
		Map<String, List<ScreenOrder>> screenOrderCustIdMap = screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getCustId));

		/*
		 * XXX 基础数据的重新自我整理包装
		 */
		// patch 单品,及数量统计
		patchSkuWithCount(screenOrderList, orderSkuList);
		// double totalMoney; 订单的总金额
		patchTotalMoney(screenOrderList, agreementList);
		// // String lng, lat; // 经纬度 boolean isNewCustomer = false;
		// 是否为新客户,custId
		patchLngLatAndIsNewCustomer(screenOrderList, custList);
		// province city areas info
		patchScreenOrderGEOInfo(screenOrderList, provinceMap, cityMap, areaMap);
		// cust -> path area,servicer
		for (ScreenCust sc : screenCustList) {
			List<ScreenOrder> oList = screenOrderCustIdMap.get(sc.getCust().getId());
			if (null != oList && oList.size() > 0) {
				sc.setProvince(oList.get(0).getProvince());
				sc.setCity(oList.get(0).getCity());
				sc.setArea(oList.get(0).getArea());
				sc.setServicerId(oList.get(0).getServicerId());
				sc.setNewCust(oList.get(0).isNewCustomer());
			}
		}

		// GROUP BY SERVERID
		Map<String, List<ScreenOrder>> servicerScreenOrderListMap = screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getServicerId));

		List<ServiceOrg> orgList = mapper.scServiceOrgList("0,2", null, false);
		Map<String, ServiceOrg> idServiceOrgMap = orgList.stream()
				.collect(Collectors.toMap(ServiceOrg::getId, Function.identity(), (o, n) -> n));
		/*
		 * XXX 计算并更新缓存中的结果
		 */
		Map<String, ScheduleBean> servicerScheduleMap = ScreenDataHolder.servicerScheduleMap; // 服务商调度大屏
		Map<String, ServicerDataBean> servicerMap = Maps.newHashMap();
		// 当日销售金额,及台数 op
		SumBean todayMoneySum = buildSellingMoney(end, screenOrderList);
		// server
		buildServicerSellingMoney(end, servicerScreenOrderListMap, servicerMap);
		// patch serviceOrgInfo
		for (String sId : idServiceOrgMap.keySet()) {
			ServicerDataBean bean = servicerMap.get(sId);
			if (bean == null) {
				bean = new ServicerDataBean();
				servicerMap.put(sId, bean);
			}
			bean.setServicer(idServiceOrgMap.get(sId));
		}

		// 昨日销售金额及环比
		List<ScreenOrder> screenOrderListYesterday = buildYesterdayDigital(mapper, dtStart, dtEnd);
		patchTotalMoney(screenOrderListYesterday);
		buildYesterdaySellingMoney(todayMoneySum, screenOrderListYesterday);
		// servicer
		buildServicerYesterdaySellingMoney(servicerMap, screenOrderListYesterday);

		// init schedule mapper
		for (String sId : servicerScreenOrderListMap.keySet()) {
			ScheduleBean schedule = servicerScheduleMap.get(sId);
			if (schedule == null) {
				servicerScheduleMap.put(sId, new ScheduleBean());
			}
		}

		// 热销产品
		List<ShuffingListHotProduct> hotList = buildHotList(screenOrderList, 10);
		ScreenDataHolder.operatorData.setHotList(hotList);
		// servicer
		for (String sId : servicerScreenOrderListMap.keySet()) {
			List<ScreenOrder> list = servicerScreenOrderListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			data.setHotList(buildHotList(list, 5));
		}

		// 销售金额 和 设备 -气泡
		List<RespiratoryBubble> moneyBubbleListTmp = Lists.newArrayList();
		List<RespiratoryBubble> devicesCountBubbleListTmp = Lists.newArrayList();
		Map<String, List<ScreenOrder>> screenOrderProvinceMap = screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getProvince));
		screenOrderProvinceMap.forEach((province, list) -> {
			SumBean skuSum = new SumBean();
			SumBean moneySum = new SumBean();
			for (ScreenOrder order : list) {
				skuSum.addLong(order.getSkuCount());
				moneySum.addDouble(order.getTotalMoney());
			}
			String lng = list.get(0).getLng();
			String lat = list.get(0).getLat();
			if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
				// 116.407851,39.91408
				lng = "116.407851";
				lat = "39.91408";
			}
			if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
				lng = Double.toString(NumberUtils.toDouble(lng) - 0.3);
				lat = Double.toString(NumberUtils.toDouble(lat) - 0.3);
			}
			moneyBubbleListTmp.add(new RespiratoryBubble(lng, lat, moneySum.getSumDouble(), 1));

			devicesCountBubbleListTmp
					.add(new RespiratoryBubble(list.get(0).getLng(), list.get(0).getLat(), skuSum.getSumDouble(), 1));
		});
		ScreenDataHolder.operatorData.setBubbleMoney(moneyBubbleListTmp);
		ScreenDataHolder.operatorData.setBubbleDevice(devicesCountBubbleListTmp);
		// 服务商的气泡 servicer
		buildServicerMoneyAndDevicesBubbles(screenOrderList, servicerMap);

		// 城市排行 热销&设备
		List<HorizontalBasicBarGraph> topSellingCities = Lists.newArrayList();
		List<HorizontalBasicBarGraph> topCitiesOfDevice = Lists.newArrayList();
		buildTopCitiesOfSellingAndDevicesList(screenOrderList, topSellingCities, topCitiesOfDevice);
		ScreenDataHolder.operatorData.setTopSellingCities(topSellingCities);
		ScreenDataHolder.operatorData.setTopCitiesOfDevice(topCitiesOfDevice);
		// servicer
		buildServicerTopCitiesOfSelliingAndDevices(servicerScreenOrderListMap, servicerMap);

		// 城市排行 会员
		List<ScreenMember> memberList = mapper.scMembers(dtStart.toDate());
		List<HorizontalBasicBarGraph> guiMemberRank = buildTopCitiesOfNewMembers(memberList, 1);
		ScreenDataHolder.operatorData.setTopCitiesOfNewCustomer(guiMemberRank);
		// servicer 是否要计算服务商的区域排名
		Map<String, List<ScreenMember>> servicerMemberMap = memberList.stream()
				.filter(mem -> mem.getServicerId() != null).collect(Collectors.groupingBy(ScreenMember::getServicerId));
		for (String sId : servicerMemberMap.keySet()) {
			List<ScreenMember> list = servicerMemberMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			data.setTopAreasOfNewCustomer(buildTopCitiesOfNewMembers(list, 2));
		}
		// 城市排行 新增注册工程师 engineer 注册工程师排名
		List<HorizontalBasicBarGraph> topCitiesOfNewEngineer = Lists.newArrayList();
		List<Engineer> engineers = mapper.scEngineerList(start, end, null);
		Map<String, List<Engineer>> cityEngineersMap = Maps.newHashMap();
		for (Engineer e : engineers) {
			String city = "";
			ServiceOrg org = idServiceOrgMap.get(e.getServicerId());
			if (org != null) {
				city = cityMap.get(org.getAddrCityCode()).getCity();
			}
			if (StringUtils.isEmpty(city)) {
				continue;
			}
			List<Engineer> list = cityEngineersMap.get(city);
			if (list == null) {
				list = Lists.newArrayList();
				cityEngineersMap.put(city, list);
			}
			list.add(e);
		}
		for (String city : cityEngineersMap.keySet()) {
			topCitiesOfNewEngineer.add(new HorizontalBasicBarGraph(city, cityEngineersMap.get(city).size() + "", "1"));
		}

		Collections.sort(topCitiesOfNewEngineer,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		ScreenDataHolder.operatorData.setTopCitiesOfNewEngineer(topCitiesOfNewEngineer);

		// 城市排行 任务数 workOrder 城市信息及类别
		List<ScreenWorkOrderOpr> oprList = mapper.scWorkOrderOprList(start, end);
		Map<String, List<ScreenWorkOrderOpr>> servicerIdScreenWorkOrderOprListMap = oprList.stream()
				.collect(Collectors.groupingBy(ScreenWorkOrderOpr::getServicerId));

		ScreenDataHolder.operatorData.setTopCitiesOfTask(buildTopCitesOfTasksRank(oprList));
		// servicer
		buildTopAreasOfTask(servicerIdScreenWorkOrderOprListMap, servicerMap);
		// servicer-schedule task count today and yesterday
		scheduleTasksCount(dt, servicerIdScreenWorkOrderOprListMap);

		Map<String, Dictionary> dic = dicMap.get(DictionaryEnum.WORK_ORDER_CATEGORY.value());
		// 任务进展 workOrder
		List<HorizontalCapsuleBarGraph> taskStatus = Lists.newArrayList();
		buildWorkOrderStatusList(dic, taskStatus, oprList);
		ScreenDataHolder.operatorData.setTaskStatus(taskStatus);
		// servicer
		for (String sId : servicerIdScreenWorkOrderOprListMap.keySet()) {
			List<ScreenWorkOrderOpr> list = servicerIdScreenWorkOrderOprListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			List<HorizontalCapsuleBarGraph> statusList = Lists.newArrayList();
			buildWorkOrderStatusList(dic, statusList, list);
			data.setTaskStatus(statusList);
		}

		// 全包, 新客户, PC来源 数量统计 XXX 用字典完善
		buildOperatorRentCustomerSourceRatio(screenOrderList);
		// servicer
		buildServicerSourceCustomersRatio(servicerScreenOrderListMap, servicerMap);

		// 飞线
		buildOperatorFlyLines(screenOrderList);
		// servicer
		for (String sId : servicerScreenOrderListMap.keySet()) {
			ServiceOrg org = idServiceOrgMap.get(sId);
			List<ScreenOrder> list = servicerScreenOrderListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			List<FlyLine> flyLines = Lists.newArrayList();
			for (ScreenOrder so : list) {
				if (org == null) {
					flyLines.add(new FlyLine(so.getLng() + "," + so.getLat(), so.getArea()));
					continue;
				}
				flyLines.add(new FlyLine(org.getLng() + "," + org.getLat(), org.getAddrCityCode(),
						so.getLng() + "," + so.getLat(), so.getArea()));
			}
			data.setFlyLines(flyLines);
		}
		ScreenDataHolder.servicerDataMap = servicerMap;
		logger.info("<<<< Initializing screen data  compeleted! {}", wt);
	}

	// XXX 电话安装的计算不准确
	private List<HorizontalBasicBarGraph> buildTopCitesOfTasksRank(List<ScreenWorkOrderOpr> screenWorkOrderList) {
		List<HorizontalBasicBarGraph> topCitiesOfTask = Lists.newArrayList();
		screenWorkOrderList.stream().filter(item -> StringUtils.isNotEmpty(item.getCity()))
				.collect(Collectors.groupingBy(ScreenWorkOrderOpr::getCity)).forEach((city, woList) -> {// 城市
					woList.stream().collect(Collectors.groupingBy(ScreenWorkOrderOpr::getTaskNature)) // 分类
							.forEach((cata, wList) -> {
								topCitiesOfTask.add(new HorizontalBasicBarGraph(city, wList.size() + "", cata + ""));
							});
				});
		return topCitiesOfTask;
	}

	private void buildTopAreasOfTask(Map<String, List<ScreenWorkOrderOpr>> servicerIdScreenWorkOrderListMap,
			Map<String, ServicerDataBean> servicerMap) {
		for (String sId : servicerIdScreenWorkOrderListMap.keySet()) {
			List<ScreenWorkOrderOpr> list = servicerIdScreenWorkOrderListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				data = new ServicerDataBean();
				servicerMap.put(sId, data);
			}
			List<HorizontalBasicBarGraph> topAreasOfTask = Lists.newArrayList();

			list.stream().filter(item -> item.getAreaCounty() != null)
					.collect(Collectors.groupingBy(ScreenWorkOrderOpr::getAreaCounty)).forEach((area, woList) -> {// area
						woList.stream().filter(opr -> opr.getTaskNature() != null)
								.collect(Collectors.groupingBy(ScreenWorkOrderOpr::getTaskNature)) // 分类 XXX 未做电话特殊处理
								.forEach((cata, wList) -> {
									topAreasOfTask.add(new HorizontalBasicBarGraph(area, wList.size() + "", cata + ""));
								});
					});
			data.setTopAreasOfTask(topAreasOfTask);
		}
	}

	private void scheduleTasksCount(DateTime dt,
			Map<String, List<ScreenWorkOrderOpr>> servicerIdScreenWorkOrderListMap) {
		for (String sId : servicerIdScreenWorkOrderListMap.keySet()) {
			// Schedule task count
			ScheduleBean schedule = ScreenDataHolder.servicerScheduleMap.get(sId);
			if (schedule == null) {
				schedule = new ScheduleBean();
				ScreenDataHolder.servicerScheduleMap.put(sId, schedule);
			}
			schedule.setTaskCount(new DigitalFlop("当前任务数", servicerIdScreenWorkOrderListMap.get(sId).size()));
		}
		// Schedule task count yesterday
		Map<String, List<WorkOrder>> sIdWorkOrderYesterdayListMap = Maps.newHashMap();
		List<WorkOrder> workOrderYesterdayList = mapper.scWorkOrderList(//
				dt.withTimeAtStartOfDay().minusDays(1).toString(DatePattern.YYYYMMDDHHMMSS.val())//
				, dt.minusDays(1).toString(DatePattern.YYYYMMDDHHMMSS.val())//
		);
		for (WorkOrder wo : workOrderYesterdayList) {
			if (wo.getServicerId() == null) {
				continue;
			}
			List<WorkOrder> list = sIdWorkOrderYesterdayListMap.get(wo.getServicerId());
			if (list == null) {
				list = Lists.newArrayList();
				sIdWorkOrderYesterdayListMap.put(wo.getServicerId(), list);
			}
			list.add(wo);
		}
		for (String sId : sIdWorkOrderYesterdayListMap.keySet()) {
			// Schedule task count
			int yesterdayCount = 0, todayCount = 0;
			ScheduleBean schedule = ScreenDataHolder.servicerScheduleMap.get(sId);
			if (schedule == null) {
				schedule = new ScheduleBean();
				ScreenDataHolder.servicerScheduleMap.put(sId, schedule);
			} else {
				if (schedule.getTaskCount() != null) {
					todayCount = (int) schedule.getTaskCount().getValue();
				} else {
					todayCount = 0;
				}
			}
			List<WorkOrder> lst = sIdWorkOrderYesterdayListMap.get(sId);
			yesterdayCount = lst.size();
			schedule.setTaskCountYesterday(new MultiText("昨日:" + yesterdayCount + "\t环比:"
					+ NumberFormat.formatDecimalStr((double) todayCount * 100d / (double) yesterdayCount)));
		}
	}

	private SumBean buildYesterdaySellingMoney(SumBean todayMoneySum, List<ScreenOrder> screenOrderListYesterday) {
		SumBean yesterdayMoneySum = new SumBean();
		SumBean yesterdaySkuNum = new SumBean();
		sumMoneySku(screenOrderListYesterday, yesterdayMoneySum, yesterdaySkuNum);
		BigDecimal growth = null;
		if (yesterdayMoneySum.getSumDouble() == 0d) {
			// growth = new BigDecimal(todayMoneySum.getSumDouble() * 100d);
			growth = new BigDecimal(0d);
		} else {
			growth = new BigDecimal((todayMoneySum.getSumDouble() - yesterdayMoneySum.getSumDouble()) * 100d
					/ (yesterdayMoneySum.getSumDouble()));
		}
		ScreenDataHolder.operatorData.setYesterday(new MultiText("前三月销售金额"
				+ NumberFormat.formatDecimalStr(yesterdayMoneySum.getSumDouble()) + "元," + yesterdaySkuNum.getSum()
				+ "台 环比:" + growth.setScale(2, RoundingMode.HALF_UP).doubleValue() + "%"));
		return yesterdaySkuNum;
	}

	private void buildServicerYesterdaySellingMoney(Map<String, ServicerDataBean> servicerMap,
			List<ScreenOrder> screenOrderListYesterday) {
		Map<String, List<ScreenOrder>> servicerScreenOrderListYesterdayMap = screenOrderListYesterday.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getServicerId));
		for (String sId : servicerMap.keySet()) {
			ServicerDataBean sData = servicerMap.get(sId);
			if (sData == null || sData.getMoney() == null) {
				continue;
			}
			List<ScreenOrder> soList = servicerScreenOrderListYesterdayMap.get(sId);
			SumBean yMoneySum = new SumBean();
			SumBean ySkuSum = new SumBean();
			sumMoneySku(soList, yMoneySum, ySkuSum);
			BigDecimal gw = null;
			if (yMoneySum.getSumDouble() == 0d) {
				gw = new BigDecimal(sData.getMoney().getValue() * 100d);
			} else {
				gw = new BigDecimal(
						((sData.getMoney().getValue() - yMoneySum.getSumDouble()) / yMoneySum.getSumDouble()) * 100d);
			}
			sData.setYesterday(new MultiText("前三月销售金额" + yMoneySum.getSumDouble() + "元," + ySkuSum.getSum() + "台 环比:"
					+ gw.setScale(2, RoundingMode.HALF_UP).doubleValue() + "%"));
		}
		servicerScreenOrderListYesterdayMap.clear();
	}

	private void buildServicerSellingMoney(String end, Map<String, List<ScreenOrder>> servicerScreenOrderListMap,
			Map<String, ServicerDataBean> servicerMap) {
		for (String sId : servicerScreenOrderListMap.keySet()) {
			List<ScreenOrder> soList = servicerScreenOrderListMap.get(sId);
			SumBean tMoneySum = new SumBean();
			SumBean tSkuSum = new SumBean();
			sumMoneySku(soList, tMoneySum, tSkuSum);
			// 首次,初始化
			ServicerDataBean servicerData = new ServicerDataBean();
			servicerMap.put(sId, servicerData);
			servicerData.setMoney(
					new DigitalFlop("近三月销售金额    " + end + "元," + tSkuSum.getSum() + "台", tMoneySum.getSumDouble()));
		}
	}

	private SumBean buildSellingMoney(String end, List<ScreenOrder> screenOrderList) {
		SumBean todayMoneySum = new SumBean();
		SumBean todaySkuNum = new SumBean();
		sumMoneySku(screenOrderList, todayMoneySum, todaySkuNum);
		ScreenDataHolder.operatorData.setMoney(
				new DigitalFlop("今日销售金额    " + end + "元," + todaySkuNum.getSum() + "台", todayMoneySum.getSumDouble()));
		return todayMoneySum;
	}

	/*
	 * XXX METHODS
	 */
	private void buildOperatorFlyLines(List<ScreenOrder> screenOrderList) {
		List<FlyLine> flyLines = Lists.newArrayList();
		for (ScreenOrder so : screenOrderList) {
			flyLines.add(new FlyLine(so.getLng() + "," + so.getLat(), so.getCity() + "," + so.getArea()));
		}
		ScreenDataHolder.operatorData.setFlyLines(flyLines);
	}

	private void buildOperatorRentCustomerSourceRatio(List<ScreenOrder> screenOrderList) {
		int newRent = 0, newCust = 0, sourcePc = 0, sourceWeChat = 0, sourceAndroid = 0, sourceIos = 0, sourceAPad = 0,
				sourceIPad = 0;
		for (ScreenOrder so : screenOrderList) {
			if (so.getOrder().getTradeType() == 0) {
				newRent++;
			}
			if (so.getOrder().isNew()) {
				newCust++;
			}
			if (so.getOrder().getOrderSource() != null) {
				if (so.getOrder().getOrderSource() == 0) {
					sourcePc++;
				} else if (so.getOrder().getOrderSource() == 1) {
					sourceWeChat++;
				} else if (so.getOrder().getOrderSource() == 2) {
					sourceAndroid++;
				} else if (so.getOrder().getOrderSource() == 3) {
					sourceIos++;
				} else if (so.getOrder().getOrderSource() == 4) {
					sourceAPad++;
				} else if (so.getOrder().getOrderSource() == 5) {
					sourceIPad++;
				}
			}
		}

		// 全半包比
		ScreenDataHolder.operatorData
				.setRentalP(Arrays.asList(new LabelComparisonPieChart("全包", Integer.toString(newRent)),
						new LabelComparisonPieChart("租赁", Integer.toString(screenOrderList.size() - newRent))));
		// 会员比
		ScreenDataHolder.operatorData
				.setNewOldCustomers(Arrays.asList(new StripPieChart("新客户", Integer.toString(newCust)),
						new StripPieChart("老客户", Integer.toString(screenOrderList.size() - newCust))));
		// 来源比
		ScreenDataHolder.operatorData.setSourceP(Arrays.asList(//
				new ShuffingPieChart("PC端", sourcePc)//
				, new ShuffingPieChart("微信", sourceWeChat)//
				, new ShuffingPieChart("安卓", sourceAndroid)//
				, new ShuffingPieChart("IOS", sourceIos)//
				, new ShuffingPieChart("aPad", sourceAPad)//
				, new ShuffingPieChart("iPad", sourceIPad)//
				, new ShuffingPieChart("其它", screenOrderList.size() - sourcePc - sourceWeChat - sourceAndroid
						- sourceIos - sourceAPad - sourceIPad)//
				, new ShuffingPieChart("移动端", screenOrderList.size() - sourcePc)//

		));
	}

	private void buildServicerSourceCustomersRatio(Map<String, List<ScreenOrder>> servicerScreenOrderListMap,
			Map<String, ServicerDataBean> servicerMap) {
		for (String sId : servicerScreenOrderListMap.keySet()) {
			List<ScreenOrder> list = servicerScreenOrderListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			int nwCust = 0, srPc = 0, sourceWeChat = 0, sourceAndroid = 0, sourceIos = 0, sourceAPad = 0,
					sourceIPad = 0;
			for (ScreenOrder so : list) {
				if (so.getOrder().isNew()) {
					nwCust++;
				}
				if (so.getOrder().getOrderSource() != null) {
					if (so.getOrder().getOrderSource() == 0) {
						srPc++;
					} else if (so.getOrder().getOrderSource() == 1) {
						sourceWeChat++;
					} else if (so.getOrder().getOrderSource() == 2) {
						sourceAndroid++;
					} else if (so.getOrder().getOrderSource() == 3) {
						sourceIos++;
					} else if (so.getOrder().getOrderSource() == 4) {
						sourceAPad++;
					} else if (so.getOrder().getOrderSource() == 5) {
						sourceIPad++;
					}
				}
			}
			// 会员比
			data.setNewOldCustomers(Arrays.asList(new ShuffingPieChart("新客户", nwCust),
					new ShuffingPieChart("老客户", list.size() - nwCust)));
			// 来源比
			data.setSourceP(Arrays.asList(//
					new ShuffingPieChart("PC端", srPc)//
					, new ShuffingPieChart("微信", sourceWeChat)//
					, new ShuffingPieChart("安卓", sourceAndroid)//
					, new ShuffingPieChart("IOS", sourceIos)//
					, new ShuffingPieChart("aPad", sourceAPad)//
					, new ShuffingPieChart("iPad", sourceIPad)//
					, new ShuffingPieChart("其它",
							list.size() - srPc - sourceWeChat - sourceAndroid - sourceIos - sourceAPad - sourceIPad)//
					, new ShuffingPieChart("移动端", list.size() - srPc)//
			));
		}
	}

	private void buildWorkOrderStatusList(Map<String, Dictionary> dic, List<HorizontalCapsuleBarGraph> taskStatus,
			List<ScreenWorkOrderOpr> oprList) {
		Map<String, List<ScreenWorkOrderOpr>> cataOprListMap = Maps.newHashMap();
		for (ScreenWorkOrderOpr opr : oprList) {
			Integer n = opr.getTaskNature();
			Integer t = opr.getRecommendedFirstTreatment();
			Integer status = n;
			if (t != null && t == 0) {
				status = 10; // 即电话支持
			}
			List<ScreenWorkOrderOpr> lst = cataOprListMap.get(status.toString());
			if (lst == null) {
				lst = Lists.newArrayList();
				cataOprListMap.put(Integer.toString(status), lst);
			}
			lst.add(opr);
		}
		// patch result
		for (String status : cataOprListMap.keySet()) {
			String cataName = null;
			if (status.equals("10")) {
				cataName = "电话支持";
			} else if (dic != null && dic.get(status) != null) {
				cataName = dic.get(status).getName();
			}
			List<ScreenWorkOrderOpr> lst = cataOprListMap.get(status);
			if (lst == null) {
				continue;
			}
			taskStatus.add(new HorizontalCapsuleBarGraph(cataName, lst.size() + "", status + ""));
		}
	}

	/**
	 * 
	 * @param memberList
	 * @param groupBy
	 *            1:city, 2:country
	 * @return
	 */
	private List<HorizontalBasicBarGraph> buildTopCitiesOfNewMembers(List<ScreenMember> memberList, int groupBy) {
		// build city Map
		Map<String, List<ScreenMember>> cityMap = Maps.newHashMap();
		for (ScreenMember mem : memberList) {
			String area = null;
			if (groupBy == 1) {
				area = mem.getCity();
			} else {
				area = mem.getCountry();
			}
			if (StringUtils.isEmpty(area)) {
				area = "未知";
			}
			List<ScreenMember> lst = cityMap.get(area);
			if (lst == null) {
				lst = Lists.newArrayList();
				cityMap.put(area, lst);
			}
			lst.add(mem);
		}
		//
		List<HorizontalBasicBarGraph> topCitiesOfNewCustomer = Lists.newArrayList();
		for (String city : cityMap.keySet()) {
			List<ScreenMember> lst = cityMap.get(city);
			topCitiesOfNewCustomer.add(new HorizontalBasicBarGraph(city, lst.size() + "", "1"));
		}
		//
		Collections.sort(topCitiesOfNewCustomer, new Comparator<HorizontalBasicBarGraph>() {
			@Override
			public int compare(HorizontalBasicBarGraph o1, HorizontalBasicBarGraph o2) {
				if (NumberUtils.isNumber(o2.getY()) && NumberUtils.isNumber(o1.getY())) {
					return NumberUtils.toInt(o2.getY()) - NumberUtils.toInt(o1.getY());
				}
				return 0;
			}
		});
		return topCitiesOfNewCustomer;
	}

	private void buildServicerTopCitiesOfSelliingAndDevices(Map<String, List<ScreenOrder>> servicerScreenOrderListMap,
			Map<String, ServicerDataBean> servicerMap) {
		for (String sId : servicerScreenOrderListMap.keySet()) {
			List<ScreenOrder> list = servicerScreenOrderListMap.get(sId);
			ServicerDataBean data = servicerMap.get(sId);
			if (data == null) {
				continue;
			}
			List<HorizontalBasicBarGraph> tSellingCities = Lists.newArrayList();
			List<HorizontalBasicBarGraph> tCitiesOfDevice = Lists.newArrayList();
			buildTopAreasOfSellingAndDevicesList(list, tSellingCities, tCitiesOfDevice);
			data.setTopSellingAreas(tSellingCities);
			data.setTopAreasOfDevice(tCitiesOfDevice);
		}
	}

	private void buildServicerMoneyAndDevicesBubbles(List<ScreenOrder> screenOrderList,
			Map<String, ServicerDataBean> servicerMap) {
		screenOrderList.stream()
				.collect(Collectors.groupingBy(ScreenOrder::getServicerId, Collectors.groupingBy(ScreenOrder::getArea)))
				.forEach((servicerId, sMap) -> {
					List<RespiratoryBubble> areaMoneyList = Lists.newArrayList();// 存储区域金额结果
					List<RespiratoryBubble> areaDeviceList = Lists.newArrayList();// 存储区域设备结果
					sMap.forEach((area, soList) -> {
						SumBean adSum = new SumBean(); // 统计区域设备数
						SumBean amSum = new SumBean(); // 统计区域金额
						for (ScreenOrder order : soList) {
							adSum.addLong(order.getSkuCount());
							amSum.addDouble(order.getTotalMoney());
						}
						String lng = soList.get(0).getLng();
						String lat = soList.get(0).getLat();
						if (StringUtils.isEmpty(lng) && StringUtils.isEmpty(lat)) {
							// 116.407851,39.91408
							lng = "116.407851";
							lat = "39.91408";
						}
						if (NumberUtils.isNumber(lng) && NumberUtils.isNumber(lat)) {
							lng = Double.toString(NumberUtils.toDouble(lng) - 0.01);
							lat = Double.toString(NumberUtils.toDouble(lat) - 0.01);
						}

						areaMoneyList.add(new RespiratoryBubble(lng, lat, amSum.getSumDouble(), 1));
						areaDeviceList.add(new RespiratoryBubble(soList.get(0).getLng(), soList.get(0).getLat(),
								adSum.getSum(), 1));
					});
					// sort
					Collections.sort(areaMoneyList,
							Comparator.comparing(RespiratoryBubble::getValue, Comparator.reverseOrder()));
					Collections.sort(areaDeviceList,
							Comparator.comparing(RespiratoryBubble::getValue, Comparator.reverseOrder()));
					//
					ServicerDataBean data = servicerMap.get(servicerId);
					if (data == null) {
						return;
					}
					data.setBubbleDevice(areaDeviceList);// set back
					data.setBubbleMoney(areaMoneyList);// setback
				});
	}

	private void buildTopCitiesOfSellingAndDevicesList(List<ScreenOrder> screenOrderList,
			List<HorizontalBasicBarGraph> topSellingCities, List<HorizontalBasicBarGraph> topCitiesOfDevice) {
		screenOrderList.stream().collect(Collectors.groupingBy(ScreenOrder::getCity)).forEach((city, oList) -> {
			SumBean moneySum = new SumBean();
			SumBean skuSum = new SumBean();
			sumMoneySku(oList, moneySum, skuSum);
			topSellingCities.add(new HorizontalBasicBarGraph(city, moneySum.getSumDouble() + "", "1"));
			topCitiesOfDevice.add(new HorizontalBasicBarGraph(city, skuSum.getSum() + "", "1"));
		});
		Collections.sort(topSellingCities,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		Collections.sort(topCitiesOfDevice,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
	}

	private void buildTopAreasOfSellingAndDevicesList(List<ScreenOrder> screenOrderList,
			List<HorizontalBasicBarGraph> topSellingCities, List<HorizontalBasicBarGraph> topCitiesOfDevice) {
		screenOrderList.stream().collect(Collectors.groupingBy(ScreenOrder::getArea)).forEach((city, oList) -> {
			SumBean moneySum = new SumBean();
			SumBean skuSum = new SumBean();
			sumMoneySku(oList, moneySum, skuSum);
			topSellingCities.add(new HorizontalBasicBarGraph(city, moneySum.getSumDouble() + "", "1"));
			topCitiesOfDevice.add(new HorizontalBasicBarGraph(city, skuSum.getSum() + "", "1"));
		});
		Collections.sort(topSellingCities,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
		Collections.sort(topCitiesOfDevice,
				Comparator.comparing(HorizontalBasicBarGraph::getY, Comparator.reverseOrder()));
	}

	private void sumMoneySku(List<ScreenOrder> screenOrderList, SumBean todayMoneySum, SumBean todaySkuNum) {
		if (CollectionUtils.isEmpty(screenOrderList)) {
			return;
		}
		for (ScreenOrder screenOrder : screenOrderList) {
			todayMoneySum.addDouble(screenOrder.getTotalMoney());
			todaySkuNum.addLong(screenOrder.getSkuCount());
		}
	}

	void initScreenWorkOrder(List<WorkOrder> workOrderList, List<ScreenWorkOrder> screenWorkOrderList) {
		for (WorkOrder wo : workOrderList) {
			ScreenWorkOrder sc = new ScreenWorkOrder();
			sc.setWorkOrder(wo);
			sc.setWorkOrderCategory(wo.getWorkOrderCategory());
			sc.setWorkOrderSource(wo.getWorkOrderSource());
			sc.setStatus(wo.getStatus() == null ? -1 : wo.getStatus());
			sc.setServicerId(wo.getServicerId());
			sc.setNextOpr(wo.getNextOpr() == null ? 0 : wo.getNextOpr());
			screenWorkOrderList.add(sc);
		}
	}

	void patchScreenOrderGEOInfo(List<ScreenOrder> screenOrderList, Map<String, SysProvinces> provinceMap,
			Map<String, SysCity> cityMap, Map<String, SysAreaCounty> areaMap) {
		for (ScreenOrder bean : screenOrderList) {
			try {
				bean.setProvince(provinceMap.get(bean.getOrder().getReceiptAddrProvinceCode()).getProvinces());
				bean.setCity(cityMap.get(bean.getOrder().getReceiptAddrCityCode()).getCity());
				bean.setArea(areaMap.get(bean.getOrder().getReceiptAddrCountyCode()).getAreaCounty());
				bean.setServicerId(bean.getOrder().getServicerId());
			} catch (Exception e) {
				//
				continue;
			}
		}
	}

	List<Cust> buildScreenCustList(ScreenMapper mapper, String start, String end, List<ScreenCust> screenCustList) {
		List<Cust> custList = mapper.scCustList(start, end, null);
		if (custList == null) {
			return null;
		}
		for (Cust cust : custList) {
			screenCustList.add(new ScreenCust(cust.getId(), cust));
		}
		return custList;
	}

	Map<String, Map<String, Dictionary>> buildDicMap(List<Dictionary> dicList) {
		Map<String, Map<String, Dictionary>> dicMap = Maps.newHashMap();
		dicList.stream().collect(Collectors.groupingBy(Dictionary::getType)).forEach((type, dList) -> {
			Map<String, Dictionary> m = dicMap.get(type);
			if (m == null) {
				m = Maps.newHashMap();
				dicMap.put(type, m);
			}
			for (Dictionary dic : dList) {
				m.put(dic.getCode(), dic);
			}
		});
		return dicMap;
	}

	List<ShuffingListHotProduct> buildHotList(List<ScreenOrder> screenOrderList, int num) {
		// 统计数量
		Map<String, OrderSku> skuCountMap = Maps.newHashMap();
		Map<String, Double> skuAmount = Maps.newHashMap();
		for (ScreenOrder order : screenOrderList) {
			if (order.getSkuList() == null) {
				continue;
			}
			for (OrderSku sku : order.getSkuList()) {
				OrderSku o = skuCountMap.get(sku.getId());
				if (o == null) {
					o = new OrderSku();
					o.setSkuCount(0);
					o.setId(sku.getId());
					o.setOrderNo(sku.getOrderNo());
					o.setSkuName(sku.getSkuName());
					o.setSkuBrand(sku.getSkuBrand());
					o.setSkuModel(sku.getSkuModel());
					skuCountMap.put(o.getId(), o);
				}
				o.setSkuCount(o.getSkuCount() + (sku.getSkuCount() == null ? 0 : sku.getSkuCount()));
				skuAmount.put(o.getId(), order.getOrder().getTotalAmount() == null ? 0d
						: order.getOrder().getTotalAmount().doubleValue());
			}
		}
		List<OrderSku> skuList = Lists.newArrayList();
		skuCountMap.forEach((skuId, sku) -> {
			skuList.add(sku);
		});
		Collections.sort(skuList, Comparator.comparing(OrderSku::getSkuCount, Comparator.reverseOrder()));
		List<ShuffingListHotProduct> hotList = Lists.newArrayList();
		for (int i = 0; i < (skuList.size() >= num ? num : skuList.size()); i++) {
			OrderSku sku = skuList.get(i);
			hotList.add(new ShuffingListHotProduct(sku.getSkuModel(),
					skuAmount.get(sku.getId()) == null ? 0d : skuAmount.get(sku.getId()), sku.getSkuCount()));
		}
		return hotList;
	}

	List<ScreenOrder> buildYesterdayDigital(ScreenMapper mapper, DateTime dtStart, DateTime dtEnd) {
		List<Order> orderListYesterday = mapper.scOrderList( //
				dtStart.minusMonths(3).toString(DatePattern.YYYYMMDDHHMMSS.val()), // 开始时间 XXX 有机会再收紧为1天。暂时改为3个月
				dtStart.toString(DatePattern.YYYYMMDDHHMMSS.val()), // 结束时间
				null);
		if (CollectionUtils.isEmpty(orderListYesterday)) {
			return new ArrayList<ScreenOrder>();
		}
		List<ScreenOrder> screenOrderListYesterday = initScreenOrder(orderListYesterday);
		List<Agreement> agreementListYesterday = mapper.scAgreementList(buildOrderIds(orderListYesterday), null,
				Maps.newHashMap());
		List<OrderSku> orderSkuListYesterday = mapper.scOrderSkuList(buildOrderNos(orderListYesterday));
		// patch 单品,及数量统计
		patchSkuWithCount(screenOrderListYesterday, orderSkuListYesterday);
		// double totalMoney; 订单的总金额
		patchTotalMoney(screenOrderListYesterday, agreementListYesterday);
		return screenOrderListYesterday;
	}

	List<ScreenOrder> initScreenOrder(List<Order> orderList) {
		List<ScreenOrder> screenOrderList = Lists.newArrayListWithCapacity(orderList.size());
		for (Order order : orderList) {
			ScreenOrder screenOrder = new ScreenOrder();
			screenOrder.setOrder(order);
			screenOrder.setServicerId(order.getServicerId());
			screenOrder.setCustId(order.getCustId());
			screenOrder.setDownLease(order.getDownLease());
			screenOrderList.add(screenOrder);
		}
		return screenOrderList;
	}

	void patchLngLatAndIsNewCustomer(List<ScreenOrder> screenOrderList, List<Cust> custList) {
		Map<String, List<Cust>> custIdMap = custList.stream().collect(Collectors.groupingBy(Cust::getId));
		for (ScreenOrder screenOrder : screenOrderList) {
			if (custIdMap.get(screenOrder.getOrder().getCustId()) == null) {
				// XXX 没匹配上,怎样处理
				continue;
			}
			Cust cust = custIdMap.get(screenOrder.getOrder().getCustId()).stream().findFirst().get();
			screenOrder.setLng(cust.getLng());
			screenOrder.setLat(cust.getLat());
			screenOrder.setCustId(cust.getId());
			if (cust.getCreateTime().getTime() > dtStart.withTimeAtStartOfDay().toDate().getTime()) {
				screenOrder.setNewCustomer(true);
			}
		}
	}

	void patchTotalMoney(List<ScreenOrder> screenOrderList) {
		List<Agreement> agreementList = mapper.scAgreementList(buildScreentOrderIds(screenOrderList), null,
				Maps.newHashMap());
		this.patchTotalMoney(screenOrderList, agreementList);
	}

	/**
	 * 总金额=订单的首付租期 * 合同中的租金
	 * 
	 * @param screenOrderList
	 * @param agreementList
	 */
	void patchTotalMoney(List<ScreenOrder> screenOrderList, List<Agreement> agreementList) {
		// 订单=合同
		Map<String, List<Agreement>> orderIdAgreementMap = Maps.newHashMap();
		for (Agreement ag : agreementList) {
			String orderId = ag.getTbOrderId();
			List<Agreement> agList = orderIdAgreementMap.get(orderId);
			if (agList == null) {
				agList = Lists.newArrayList();
				orderIdAgreementMap.put(orderId, agList);
			}
			agList.add(ag);
		}
		// 订单中存有金额，不必另行计算
		for (ScreenOrder screenOrder : screenOrderList) {
			Integer downLease = screenOrder.getOrder().getDownLease();
			if (downLease == null) {
				screenOrder.setTotalMoney(0d);
				continue;
			}
			List<Agreement> ags = orderIdAgreementMap.get(screenOrder.getOrder().getId());
			if (ags.size() == 0) {
				screenOrder.setTotalMoney(0d);
				continue;
			}
			BigDecimal rent = ags.get(0).getRent();
			Integer tenancy = ags.get(0).getTenancy();
			if (rent == null || tenancy == null) {
				screenOrder.setTotalMoney(0d);
				continue;
			}
			// 租金 * 租期
			Double money = rent.multiply(new BigDecimal(downLease)).divide(new BigDecimal(tenancy)).doubleValue();
			screenOrder.setTotalMoney(money);
		}
	}

	void patchSkuWithCount(List<ScreenOrder> screenOrderList, List<OrderSku> orderSkuList) {
		Map<String, List<OrderSku>> skuOrderNoMap = orderSkuList.stream()
				.collect(Collectors.groupingBy(OrderSku::getOrderNo));
		for (ScreenOrder screenOrder : screenOrderList) {
			List<OrderSku> list = skuOrderNoMap.get(screenOrder.getOrder().getOrderNo());
			if (list == null) {
				continue;
			}
			for (OrderSku orderSku : list) {
				int orderSkuCount = 0;
				if (orderSku.getSkuCount() != null) {
					orderSkuCount = orderSku.getSkuCount();
				}
				screenOrder.setSkuCount(screenOrder.getSkuCount() + orderSkuCount);
				screenOrder.getSkuList().add(orderSku);
			}
		}
	}

	String buildOrderNos(List<Order> orderList) {
		String nos = null;
		List<String> orderNos = Lists.newArrayListWithCapacity(orderList.size());
		for (Order order : orderList) {
			orderNos.add(order.getOrderNo());
		}
		nos = "'" + StringUtils.join(orderNos, "','") + "'";
		return nos;
	}

	String buildOrderIds(List<Order> orderList) {
		String ids = null;
		List<String> orderIds = Lists.newArrayListWithCapacity(orderList.size());
		for (Order order : orderList) {
			orderIds.add(order.getId());
		}
		ids = "'" + StringUtils.join(orderIds, "','") + "'";
		return ids;
	}

	String buildScreentOrderIds(List<ScreenOrder> orderList) {
		String ids = null;
		List<String> orderIds = Lists.newArrayListWithCapacity(orderList.size());
		for (ScreenOrder order : orderList) {
			orderIds.add(order.getOrder().getId());
		}
		ids = "'" + StringUtils.join(orderIds, "','") + "'";
		return ids;
	}

	enum DatePattern {
		YYYYMMDDHHMMSS("yyyy-MM-dd HH:mm:ss"), //
		YYYY_MM_DD("yyyy-MM-dd");

		String value;

		DatePattern(String value) {
			this.value = value;
		}

		public String val() {
			return this.value;
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		ScreenStarter.main(args);
	}
}
