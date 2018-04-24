package com.smartoa.service.impl.screen.handler;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.smartoa.service.ScreenStarter;
import com.smartoa.service.impl.screen.holder.ScreenDataHolder;
import com.smartoa.service.mapper.ScreenHeatsMapper;
import com.smartoa.service.model.Agreement;
import com.smartoa.service.model.Engineer;
import com.smartoa.service.model.GeoAtlas;
import com.smartoa.service.model.Order;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.SysAreaCounty;
import com.smartoa.service.model.SysCity;
import com.smartoa.service.model.SysProvinces;
import com.smartoa.service.model.WorkOrder;
import com.smartoa.service.model.WorkOrderOpr;
import com.smartoa.service.model.screen.components.AreaHeatsLayer;
import com.smartoa.service.model.screen.components.BasicLineGraph;
import com.smartoa.service.model.screen.components.BubbleGraph;
import com.smartoa.service.model.screen.components.StripPieChart;
import com.smartoa.service.model.screen.components.VerticalBasicBarGraph;
import com.smartoa.service.model.screen.components.VerticalCapsuleBarGraph;
import com.smartoa.service.model.screen.other.GeoJson;
import com.smartoa.service.model.screen.other.Row;
import com.smartoa.service.model.screen.other.ScreenDevicePrintSettlement;
import com.smartoa.service.model.screen.other.ScreenOrder;
import com.smartoa.service.model.screen.other.ScreenPackagePrint;
import com.smartoa.service.model.screen.other.ScreenPrintDaily;
import com.smartoa.service.model.screen.other.ScreenUserBill;
import com.smartoa.service.model.screen.other.ScreenWorkOrder;
import com.smartoa.service.model.screen.other.ServicerHeatsBean;

public class ScreenHeatsHandler extends ScreenDataHandlerV2 {
	private static final Logger logger = LoggerFactory.getLogger(ScreenHeatsHandler.class);
	ScreenHeatsMapper heatsMapper = (ScreenHeatsMapper) ScreenStarter.applicationContext.getBean("screenHeatsMapper");
	String taskName = "【大屏-热力后台任务】";

	public void init() {
		Stopwatch wt = Stopwatch.createStarted();
		logger.info(">>>{} Screen heat initializing...", taskName);
		/*
		 * XXX DB加载及缓冲处理
		 */
		// 加载城市区域列表
		Map<String, SysProvinces> provinceMap = mapper.scProvinceList().stream()
				.collect(Collectors.toMap(SysProvinces::getProvincesCode, Function.identity(), (o, n) -> n));
		Map<String, SysCity> cityMap = mapper.scCityList().stream()
				.collect(Collectors.toMap(SysCity::getCityCode, Function.identity(), (o, n) -> n));
		Map<String, SysAreaCounty> areaMap = mapper.scAreaList().stream()
				.collect(Collectors.toMap(SysAreaCounty::getAreaCode, Function.identity(), (o, n) -> n));
		logger.debug("{} province list size:{} cities:{} areas:{}", taskName, provinceMap.size(), cityMap.size(),
				areaMap.size());
		// 字典
		// List<Dictionary> dicList = mapper.scDicList();
		// dicMap type-code-dic
		// Map<String, Map<String, Dictionary>> dicMap = buildDicMap(dicList);

		// -- 合同 -订单 查询出来全部包含者未生效的合同 XXX 最好有一个时间范围，来缩小查询结果
		List<Agreement> agreementList = mapper.scAgreementList(null, null, Maps.newHashMap());
		logger.debug("{} agreements:{}", taskName, agreementList.size());
		Map<String, List<Agreement>> agreementOrderIdMap = agreementList.stream()
				.collect(Collectors.groupingBy(Agreement::getTbOrderId));
		Set<String> orderIdsSet = agreementOrderIdMap.keySet();
		String orderIds = "'" + StringUtils.join(orderIdsSet, "','") + "'";
		logger.debug("{} orderIds:{}", taskName, orderIds);
		List<Order> orderList = mapper.scOrderList(null, null, orderIds);
		List<ScreenOrder> screenOrderList = initScreenOrder(orderList);// 订单
		for (ScreenOrder bean : screenOrderList) {
			try {
				bean.setProvince(provinceMap.get(bean.getOrder().getReceiptAddrProvinceCode()).getProvinces());
				bean.setCity(cityMap.get(bean.getOrder().getReceiptAddrCityCode()).getCity());
				bean.setArea(areaMap.get(bean.getOrder().getReceiptAddrCountyCode()).getAreaCounty());
			} catch (Exception e) {
				continue;
			}
		}
		// 设备ID->服务商ID
		// 订单ID->合同ID XX -> 设备ID
		Map<String, Agreement> deviceIdAgreementMap = buildDeviceIdAgreementMap(agreementList);
		Map<String, ScreenOrder> orderIdScreenOrderMap = buildOrderIdScreenOrderMap(screenOrderList);
		Map<String, String> deviceIdServicerOrgIdMap = buildDeviceIdScreenOrderMap(deviceIdAgreementMap,
				orderIdScreenOrderMap);
		List<ServiceOrg> orgList = mapper.scServiceOrgList("0,2", null, false);
		Map<String, ServiceOrg> idServiceOrgMap = orgList.stream()
				.collect(Collectors.toMap(ServiceOrg::getId, Function.identity(), (o, n) -> n));

		/*
		 * XXX 元数据准备
		 */
		// 合同 -> 设备日印量
		List<ScreenPrintDaily> screenPrintDailyList = Lists.newArrayList(); // 日印量信息
		Set<String> agreementDeviceIds = Sets.newHashSet(); // 合同中的设备ID
		for (Agreement agreement : agreementList) {
			agreementDeviceIds.add(agreement.getTbDeviceId());
		}
		DateTime now = new DateTime();
		List<Map<String, Object>> printDailyListMap = Lists.newArrayList();
		buildPrintDailyListMap(now, printDailyListMap);

		for (Map<String, Object> rs : printDailyListMap) {
			ScreenPrintDaily spd = new ScreenPrintDaily(rs.get("id").toString()//
					, (Date) rs.get("dataDate") //
					, rs.get("tbDeviceId").toString()//
					, rs.get("deviceSerialNum").toString() //
					, (int) rs.get("blackCountDaily") //
					, (int) rs.get("colorCountDaily") //
			);
			// set servicerid
			spd.setServicerId(deviceIdServicerOrgIdMap.get(spd.getTbDeviceId()));
			screenPrintDailyList.add(spd);
		}
		// device->agreement::rentInfo->order::patch GEO info
		Map<String, ScreenOrder> idScreenOrderMap = buildOrderIdScreenOrderMap(screenOrderList);
		for (ScreenPrintDaily printDaily : screenPrintDailyList) {
			Agreement agreement = deviceIdAgreementMap.get(printDaily.getTbDeviceId());
			if (agreement == null) {
				continue;
			}
			ScreenOrder so = idScreenOrderMap.get(agreement.getTbOrderId());
			if (so == null) {
				continue;
			}
			printDaily.setProvince(so.getProvince());
			printDaily.setProvinceCode(so.getOrder().getReceiptAddrProvinceCode());
			printDaily.setCity(so.getCity());
			printDaily.setCityCode(so.getOrder().getReceiptAddrCityCode());
			printDaily.setArea(so.getArea());
			printDaily.setAreaCode(so.getOrder().getReceiptAddrCountyCode());
			printDaily.setRentFee(agreement.getRent().doubleValue());
		}

		// - 抄表计费
		// 设备 - 城市 - 租金 - 印量包 - 抄表 - day
		List<ScreenDevicePrintSettlement> screenDevicePrintSettlementList = buildScreenDevicePrintSettlementList(now);// 结算
		List<ScreenPackagePrint> packagePrintList = buildScreenPackagePrintingList();
		// patch 印量包,抄表数据
		Map<String, Double> deviceIdRentFeeMap = screenPrintDailyList.stream()
				.collect(Collectors.toMap(ScreenPrintDaily::getTbDeviceId, ScreenPrintDaily::getRentFee, (o, n) -> o));
		Map<String, Double> deviceIdPackageFeeMap = packagePrintList.stream().collect(
				Collectors.toMap(ScreenPackagePrint::getTbDeviceId, ScreenPackagePrint::getPrice, (o, n) -> o));
		Map<String, ScreenPrintDaily> serialNumDeviceIdMap = screenPrintDailyList.stream()
				.collect(Collectors.toMap(ScreenPrintDaily::getDeviceSerialNum, Function.identity(), (o, n) -> o));
		for (ScreenDevicePrintSettlement bean : screenDevicePrintSettlementList) {
			bean.setReadFee(
					bean.getBlackMoney() * bean.getBlackPrintUse() + bean.getColorPrintUse() * bean.getColorMoney());

			String serialNum = bean.getDeviceSerialNum();
			ScreenPrintDaily screenPrintDaily = serialNumDeviceIdMap.get(serialNum);
			if (screenPrintDaily == null) {
				continue;
			}
			bean.setProvince(screenPrintDaily.getProvince());
			bean.setCity(screenPrintDaily.getCity());
			bean.setArea(screenPrintDaily.getArea());
			String deviceId = screenPrintDaily.getTbDeviceId();
			Double rentFee = deviceIdRentFeeMap.get(deviceId);
			Double packageFee = deviceIdPackageFeeMap.get(deviceId);
			bean.setRentFee(rentFee == null ? 0 : rentFee);
			bean.setPackageFee(packageFee == null ? 0 : packageFee);
			bean.setDeviceId(deviceId);
			bean.setServicerId(deviceIdServicerOrgIdMap.get(deviceId));
		}
		// 建立省市-阿里Atlas对应关系 FIXME 这种对应，可能会与现有系统有偏差，需要后续研究解决方案
		Map<String, GeoAtlas> sysProvinceAtlasMap = Maps.newHashMap();
		Map<String, GeoAtlas> sysCityAtlasMap = Maps.newHashMap();
		buildAtlasMap(sysProvinceAtlasMap, sysCityAtlasMap);

		/*
		 * XXX 数据结果计算部分
		 */
		Map<String, ServicerHeatsBean> servicerMap = Maps.newHashMap();
		// 升序 排列
		Collections.sort(screenPrintDailyList, Comparator.comparing(ScreenPrintDaily::getDataDate));
		// A 近一年区域热力 && 总的黑白彩色比
		List<StripPieChart> blackWhiteColorRatio = Lists.newArrayList();//
		List<AreaHeatsLayer> areaHeats = Lists.newArrayList();//
		buildHeatLayerAndBlackColorRatio(screenPrintDailyList, sysProvinceAtlasMap, blackWhiteColorRatio, areaHeats);
		// servicer init FIXME sysProvinceAtlasMap 需要更换为咱们的地理位置信息
		Map<String, List<ScreenPrintDaily>> servicerIdScreenPrintDailyListMap = screenPrintDailyList.stream()
				.collect(Collectors.groupingBy(ScreenPrintDaily::getServicerId));
		buildServicerAreaHeatBAndlackColorRatio(sysProvinceAtlasMap, servicerMap, servicerIdScreenPrintDailyListMap);

		// B 近一年月均印量 及黑白彩色分段
		List<VerticalCapsuleBarGraph> monthAvg = buildMonthAvgWithBlackWhiteColor(screenPrintDailyList);
		// servicer
		buildServicerMonthAvg(servicerMap, servicerIdScreenPrintDailyListMap);

		// 近一年月印量 区域 排名 气泡
		List<BubbleGraph> monthHeatRank = buildMonthHeatRank(screenPrintDailyList, false);
		// servicer
		buildServicerMonthHeatRank(servicerMap, servicerIdScreenPrintDailyListMap);

		// 抄表计费 && 抄表计费-折线
		List<VerticalCapsuleBarGraph> meterReadingMonth = Lists.newArrayList();
		List<BasicLineGraph> meterReadingMonthLine = Lists.newArrayList();
		// FIXME 需要恢复至正常范围
		List<ScreenUserBill> billList = heatsMapper.scUserBill(now.minusYears(1).toDate(), now.toDate());
		Map<String, List<ScreenUserBill>> cityBillsMap = billList.stream()
				.filter(item -> !StringUtils.isEmpty(item.getCity()))
				.collect(Collectors.groupingBy(ScreenUserBill::getCity));
		buildMeterReading(meterReadingMonth, meterReadingMonthLine, billList, cityBillsMap);
		buildServicerMeterReadingChart(servicerMap, billList);

		// 工程师效率 近1年,每月任务任务总数, 其中认证工程师人数; 人均服务任务数
		// 工程师效率 - 折线
		List<BasicLineGraph> engineerEfficiencyLine = Lists.newArrayList();//
		List<VerticalBasicBarGraph> engineerEfficiency = Lists.newArrayList();//
		// engineerProcessList 升序,有序工程师出工排列
		// 关系数据加载
		List<WorkOrder> workOrderList = mapper.scWorkOrderList(//
				now.minusYears(1).toString(DatePattern.YYYY_MM_DD.val())//
				, now.toString(DatePattern.YYYY_MM_DD.val())//
		);
		Collections.sort(workOrderList, new Comparator<WorkOrder>() {
			@Override
			public int compare(WorkOrder o1, WorkOrder o2) {
				if (o2.getCreateTime() == null || o1.getCreateTime() == null) {
					return 0;
				}
				return o2.getCreateTime().compareTo(o1.getCreateTime());
			}
		});
		List<ScreenWorkOrder> screenWorkOrderList = buildScreenWorkOrderList(workOrderList);
		List<WorkOrderOpr> workOrderOprList = heatsMapper.scWorkOrderOprList(//
				now.minusYears(1).toString(DatePattern.YYYYMMDDHHMMSS.val())//
				, now.toString(DatePattern.YYYYMMDDHHMMSS.val())//
		);
		Map<String, List<WorkOrderOpr>> workOrderIdWorkOrderOprListMap = workOrderOprList.stream()
				.collect(Collectors.groupingBy(WorkOrderOpr::getWorkOrderId));// 工单ID->操作
		List<Engineer> engineerList = mapper.scEngineerList(null, null, null);
		Map<String, Engineer> idEngineerMap = engineerList.stream()
				.collect(Collectors.toMap(Engineer::getId, Function.identity(), (o, n) -> n));// 工程师ID
		// op
		buildEngineerWorkEfficiencyAndLines(engineerEfficiencyLine, engineerEfficiency, screenWorkOrderList,
				workOrderIdWorkOrderOprListMap, idEngineerMap);
		// servicer
		Map<String, List<ScreenWorkOrder>> servicerIdScreenWorkOrderListMap = screenWorkOrderList.stream()
				.collect(Collectors.groupingBy(ScreenWorkOrder::getServicerId));
		for (String sId : servicerIdScreenWorkOrderListMap.keySet()) {
			ServicerHeatsBean heat = servicerMap.get(sId);
			if (heat == null) {
				continue;
			}
			List<BasicLineGraph> eLine = Lists.newArrayList();//
			List<VerticalBasicBarGraph> e = Lists.newArrayList();//
			List<ScreenWorkOrder> lst = servicerIdScreenWorkOrderListMap.get(sId);
			buildEngineerWorkEfficiencyAndLines(eLine, e, lst, workOrderIdWorkOrderOprListMap, idEngineerMap);
			heat.setEngineerEfficiency(e);
			heat.setEngineerEfficiencyLine(eLine);
		}
		// patch servicerInfo
		for (String sId : idServiceOrgMap.keySet()) {
			ServicerHeatsBean bean = servicerMap.get(sId);
			if (bean == null) {
				bean = new ServicerHeatsBean();
				servicerMap.put(sId, bean);
			}
			bean.setServicer(idServiceOrgMap.get(sId));
		}
		ScreenDataHolder.servicerHeatsMap = servicerMap;
		ScreenDataHolder.operatorHeatsBean.setAreaHeats(areaHeats);
		ScreenDataHolder.operatorHeatsBean.setMeterReadingMonth(meterReadingMonth);
		ScreenDataHolder.operatorHeatsBean.setMeterReadingMonthLine(meterReadingMonthLine);
		ScreenDataHolder.operatorHeatsBean.setMonthAvg(monthAvg);
		ScreenDataHolder.operatorHeatsBean.setBlackWhiteColorRatio(blackWhiteColorRatio);
		ScreenDataHolder.operatorHeatsBean.setMonthHeatRank(monthHeatRank);
		ScreenDataHolder.operatorHeatsBean.setEngineerEfficiency(engineerEfficiency);
		ScreenDataHolder.operatorHeatsBean.setEngineerEfficiencyLine(engineerEfficiencyLine);
		logger.info("{} <<<< Initializing screen heat  compeleted! {}", taskName, wt);
	}

	private void buildServicerMeterReadingChart(Map<String, ServicerHeatsBean> servicerMap,
			List<ScreenUserBill> billList) {
		Map<String, List<ScreenUserBill>> sIdBillListMap = billList.stream()
				.filter(item -> !StringUtils.isEmpty(item.getServicerId()))
				.collect(Collectors.groupingBy(ScreenUserBill::getServicerId));
		for (String sId : sIdBillListMap.keySet()) {
			ServicerHeatsBean heat = servicerMap.get(sId);
			if (heat == null) {
				heat = new ServicerHeatsBean();
				servicerMap.put(sId, heat);
			}
			List<VerticalCapsuleBarGraph> mm = Lists.newArrayList();
			List<BasicLineGraph> mml = Lists.newArrayList();
			Map<String, List<ScreenUserBill>> areasBillsMap = billList.stream()
					.filter(item -> !StringUtils.isEmpty(item.getCountry()))
					.collect(Collectors.groupingBy(ScreenUserBill::getCountry));
			buildMeterReading(mm, mml, sIdBillListMap.get(sId), areasBillsMap);
			heat.setMeterReadingMonth(mm);
			heat.setMeterReadingMonthLine(mml);
		}
	}

	private void buildMeterReading(List<VerticalCapsuleBarGraph> meterReadingMonth,
			List<BasicLineGraph> meterReadingMonthLine, List<ScreenUserBill> billList,
			Map<String, List<ScreenUserBill>> cityBillsMap) {
		for (String city : cityBillsMap.keySet()) {
			List<ScreenUserBill> lst = cityBillsMap.get(city);
			double rent = 0d, read = 0d, pack = 0d;
			for (ScreenUserBill bill : lst) {
				rent += bill.getDeviceAmount() == null ? 0d : bill.getDeviceAmount().doubleValue();
				read += bill.getPaperAmount() == null ? 0d : bill.getPaperAmount().doubleValue();
				pack += bill.getPrintAmount() == null ? 0d : bill.getPrintAmount().doubleValue();
			}
			meterReadingMonth.add(new VerticalCapsuleBarGraph(city, Double.toString(rent), "1"));
			meterReadingMonth.add(new VerticalCapsuleBarGraph(city, Double.toString(read), "2"));
			meterReadingMonth.add(new VerticalCapsuleBarGraph(city, Double.toString(pack), "3"));
			meterReadingMonthLine.add(new BasicLineGraph(city, rent, "1"));
			meterReadingMonthLine.add(new BasicLineGraph(city, read, "2"));
			meterReadingMonthLine.add(new BasicLineGraph(city, pack, "3"));
		}
		Collections.sort(meterReadingMonth, new Comparator<VerticalCapsuleBarGraph>() {
			@Override
			public int compare(VerticalCapsuleBarGraph o1, VerticalCapsuleBarGraph o2) {
				if (StringUtils.isEmpty(o2.getY()) && StringUtils.isEmpty(o1.getY())) {
					return 0;
				}
				return o2.getY().compareTo(o1.getY());
			}
		});
		Collections.sort(meterReadingMonthLine, new Comparator<BasicLineGraph>() {
			@Override
			public int compare(BasicLineGraph o1, BasicLineGraph o2) {
				return (int) (o2.getY() - o1.getY());
			}
		});
	}

	private void buildPrintDailyListMap(DateTime now, List<Map<String, Object>> printDailyListMap) {
		List<Map<String, Object>> lastYearList = heatsMapper.scPrintDaily(now.toString("yyyy")//
				, now.minusYears(1).toDate()//
				, now.toDate()//
				, null);
		List<Map<String, Object>> thisYearListMap = heatsMapper.scPrintDaily(now.minusYears(1).toString("yyyy")//
				, now.minusYears(1).toDate()//
				, now.toDate()//
				, null);
		printDailyListMap.addAll(lastYearList);
		printDailyListMap.addAll(thisYearListMap);
	}

	private List<ScreenWorkOrder> buildScreenWorkOrderList(List<WorkOrder> workOrderList) {
		List<ScreenWorkOrder> screenWorkOrderList = Lists.newArrayList();
		for (WorkOrder wo : workOrderList) {
			ScreenWorkOrder swo = new ScreenWorkOrder();
			swo.setWorkOrder(wo);
			swo.setServicerId(wo.getServicerId());
			screenWorkOrderList.add(swo);
		}
		return screenWorkOrderList;
	}

	private void buildEngineerWorkEfficiencyAndLines(List<BasicLineGraph> engineerEfficiencyLine,
			List<VerticalBasicBarGraph> engineerEfficiency, List<ScreenWorkOrder> workOrderList,
			Map<String, List<WorkOrderOpr>> workOrderIdWorkOrderOprListMap, Map<String, Engineer> idEngineerMap) {
		Map<String, List<ScreenWorkOrder>> monthWorkOrderListMap = Maps.newHashMap();
		for (ScreenWorkOrder wo : workOrderList) {
			String monthKey = new DateTime(wo.getWorkOrder().getCreateTime()).toString("yy-MM");
			List<ScreenWorkOrder> taskList = monthWorkOrderListMap.get(monthKey);
			if (taskList == null) {
				taskList = Lists.newArrayList();
				monthWorkOrderListMap.put(monthKey, taskList);
			}
			taskList.add(wo);
		}
		// 计算平均每月任务数,工程师数(其中的认工程师人数), 平均任务数/人
		for (String monthKey : monthWorkOrderListMap.keySet()) {
			List<ScreenWorkOrder> taskList = monthWorkOrderListMap.get(monthKey);
			int taskCount = taskList.size();// 任务数量
			List<WorkOrderOpr> oprList = Lists.newArrayList();
			for (ScreenWorkOrder wo : taskList) {
				List<WorkOrderOpr> tmp = workOrderIdWorkOrderOprListMap.get(wo.getWorkOrder().getId());
				if (tmp == null) {
					continue;
				}
				oprList.addAll(tmp);
			}
			int enCount = 0; // 工程师数量
			Set<String> enIds = Sets.newHashSet();
			for (WorkOrderOpr opr : oprList) {
				String enId = opr.getEngineerId();
				if (StringUtils.isEmpty(enId)) {
					continue;
				}
				enIds.add(enId);
			}
			enCount = enIds.size();

			double avgTask = 0;
			if (enCount != 0) {
				avgTask = (double) taskCount / (double) enCount; // 人均任务数
			}
			int authEnCount = 0;// 认证工程师数量
			for (String enId : enIds) {
				Engineer en = idEngineerMap.get(enId);
				if (en == null) {
					continue;
				}
				if (en.getIfAuthentication().equals((short) 1)) {
					authEnCount++;
				}
			}
			engineerEfficiencyLine.add(new BasicLineGraph(monthKey, avgTask, "1"));
			engineerEfficiency.add(new VerticalBasicBarGraph(monthKey, Integer.toString(taskCount), "1"));
			engineerEfficiency.add(new VerticalBasicBarGraph(monthKey, Integer.toString(authEnCount), "2"));
		}
	}


	private void buildServicerMonthHeatRank(Map<String, ServicerHeatsBean> servicerMap,
			Map<String, List<ScreenPrintDaily>> servicerIdScreenPrintDailyListMap) {
		for (String sId : servicerIdScreenPrintDailyListMap.keySet()) {
			ServicerHeatsBean heat = servicerMap.get(sId);
			if (heat == null) {
				continue;
			}
			List<ScreenPrintDaily> spdList = servicerIdScreenPrintDailyListMap.get(sId);
			heat.setMonthHeatRank(buildMonthHeatRank(spdList, true));
		}
	}

	private List<BubbleGraph> buildMonthHeatRank(List<ScreenPrintDaily> screenPrintDailyList, boolean ifArea) {
		List<BubbleGraph> monthHeatRank = Lists.newArrayList();

		Map<String, List<ScreenPrintDaily>> cityPrintListMap = null;
		if (ifArea) {
			cityPrintListMap = screenPrintDailyList.stream().collect(Collectors.groupingBy(ScreenPrintDaily::getArea));
		} else {
			cityPrintListMap = screenPrintDailyList.stream().collect(Collectors.groupingBy(ScreenPrintDaily::getCity));
		}
		for (String city : cityPrintListMap.keySet()) {
			int printCount = 0;
			List<ScreenPrintDaily> lst = cityPrintListMap.get(city);
			printCount += lst.stream().mapToInt(ScreenPrintDaily::getBlackCountDaily).sum();
			printCount += lst.stream().mapToInt(ScreenPrintDaily::getColorCountDaily).sum();
			printCount = printCount / 12;
			monthHeatRank.add(new BubbleGraph(city, Integer.toString(printCount), Double.toString(printCount / 4d)));
		}
		Collections.sort(monthHeatRank, new Comparator<BubbleGraph>() {
			@Override
			public int compare(BubbleGraph o1, BubbleGraph o2) {
				if (o1 == null || o2 == null) {
					return 0;
				}
				return o1.getY().compareTo(o2.getY());
			}
		});
		return monthHeatRank;
	}

	private void buildServicerMonthAvg(Map<String, ServicerHeatsBean> servicerMap,
			Map<String, List<ScreenPrintDaily>> servicerIdScreenPrintDailyListMap) {
		for (String sId : servicerIdScreenPrintDailyListMap.keySet()) {
			ServicerHeatsBean heat = servicerMap.get(sId);
			if (heat == null) {
				continue;
			}
			List<ScreenPrintDaily> spdList = servicerIdScreenPrintDailyListMap.get(sId);
			List<VerticalCapsuleBarGraph> mAvg = buildMonthAvgWithBlackWhiteColor(spdList);
			heat.setMonthAvg(mAvg);
		}
	}

	private List<VerticalCapsuleBarGraph> buildMonthAvgWithBlackWhiteColor(
			List<ScreenPrintDaily> screenPrintDailyList) {
		List<VerticalCapsuleBarGraph> monthAvg = Lists.newArrayList();//
		Map<String, List<ScreenPrintDaily>> monthPrintMap = Maps.newLinkedHashMap();
		for (ScreenPrintDaily spd : screenPrintDailyList) {
			String monthKey = new DateTime(spd.getDataDate()).toString("yy年MM月");
			List<ScreenPrintDaily> list = monthPrintMap.get(monthKey);
			if (list == null) {
				list = Lists.newArrayList();
				monthPrintMap.put(monthKey, list);
			}
			list.add(spd);
		}
		for (String monthKey : monthPrintMap.keySet()) {
			int black = 0;
			int color = 0;
			for (ScreenPrintDaily d : monthPrintMap.get(monthKey)) {
				black += d.getBlackCountDaily();
				color += d.getColorCountDaily();
			}
			monthAvg.add(new VerticalCapsuleBarGraph(monthKey, Integer.toString(black), "1"));
			monthAvg.add(new VerticalCapsuleBarGraph(monthKey, Integer.toString(color), "2"));
		}
		return monthAvg;
	}

	private void buildServicerAreaHeatBAndlackColorRatio(Map<String, GeoAtlas> sysProvinceAtlasMap,
			Map<String, ServicerHeatsBean> servicerMap,
			Map<String, List<ScreenPrintDaily>> servicerIdScreenPrintDailyListMap) {
		for (String sId : servicerIdScreenPrintDailyListMap.keySet()) {
			ServicerHeatsBean heat = servicerMap.get(sId);
			if (heat == null) {
				heat = new ServicerHeatsBean();
				servicerMap.put(sId, heat);
			}
			List<ScreenPrintDaily> spdList = servicerIdScreenPrintDailyListMap.get(sId);

			List<StripPieChart> blackWhiteColorRatio = Lists.newArrayList();//
			List<AreaHeatsLayer> areaHeats = Lists.newArrayList();//
			int blackCount = 0, colorCount = 0;
			Map<String, List<ScreenPrintDaily>> provinceScreenPrintDailyListMap = spdList.stream()
					.filter(item -> item.getAreaCode() != null)
					.collect(Collectors.groupingBy(ScreenPrintDaily::getAreaCode));
			for (String key : provinceScreenPrintDailyListMap.keySet()) {
				// GeoAtlas atlas = sysProvinceAtlasMap.get(key);
				// if (atlas == null) {
				// continue;
				// }
				if (StringUtils.isEmpty(key)) {
					continue;
				}
				double total = 0d;
				for (ScreenPrintDaily daily : provinceScreenPrintDailyListMap.get(key)) {
					total += daily.getBlackCountDaily() + daily.getColorCountDaily();
					blackCount += daily.getBlackCountDaily();
					colorCount += daily.getColorCountDaily();
				}
				if (total > 0d) {
					// areaHeats.add(new AreaHeatsLayer(atlas.getAdcode(), total));
					areaHeats.add(new AreaHeatsLayer(key, total));
				}
			}
			blackWhiteColorRatio.add(new StripPieChart("黑白", Integer.toString(blackCount)));
			blackWhiteColorRatio.add(new StripPieChart("彩色", Integer.toString(colorCount)));

			heat.setAreaHeats(areaHeats);
			heat.setBlackWhiteColorRatio(blackWhiteColorRatio);
		}
	}

	private void buildHeatLayerAndBlackColorRatio(List<ScreenPrintDaily> screenPrintDailyList,
			Map<String, GeoAtlas> sysProvinceAtlasMap, List<StripPieChart> blackWhiteColorRatio,
			List<AreaHeatsLayer> areaHeats) {
		int blackCount = 0, colorCount = 0;
		Map<String, List<ScreenPrintDaily>> provinceScreenPrintDailyListMap = screenPrintDailyList.stream()
				.collect(Collectors.groupingBy(ScreenPrintDaily::getProvince));
		for (String key : provinceScreenPrintDailyListMap.keySet()) {
			GeoAtlas atlas = sysProvinceAtlasMap.get(key);
			if (atlas == null) {
				continue;
			}
			double total = 0d;
			for (ScreenPrintDaily daily : provinceScreenPrintDailyListMap.get(key)) {
				total += daily.getBlackCountDaily() + daily.getColorCountDaily();
				blackCount += daily.getBlackCountDaily();
				colorCount += daily.getColorCountDaily();
			}
			if (total > 0d) {
				areaHeats.add(new AreaHeatsLayer(atlas.getAdcode(), total));
			}
		}
		blackWhiteColorRatio.add(new StripPieChart("黑白", Integer.toString(blackCount)));
		blackWhiteColorRatio.add(new StripPieChart("彩色", Integer.toString(colorCount)));
	}

	private Map<String, String> buildDeviceIdScreenOrderMap(Map<String, Agreement> deviceIdAgreementMap,
			Map<String, ScreenOrder> orderIdScreenOrderMap) {
		Map<String, String> deviceIdServicerOrgId = Maps.newHashMap();
		for (String deviceId : deviceIdAgreementMap.keySet()) {
			Agreement agreement = deviceIdAgreementMap.get(deviceId);
			if (agreement == null) {
				continue;
			}
			ScreenOrder so = orderIdScreenOrderMap.get(agreement.getTbOrderId());
			if (so == null) {
				continue;
			}
			deviceIdServicerOrgId.put(deviceId, so.getServicerId());
		}
		return deviceIdServicerOrgId;
	}

	private Map<String, ScreenOrder> buildOrderIdScreenOrderMap(List<ScreenOrder> screenOrderList) {
		Map<String, ScreenOrder> orderIdScreenOrderMap = Maps.newHashMap();
		for (ScreenOrder so : screenOrderList) {
			orderIdScreenOrderMap.put(so.getOrder().getId(), so);
		}
		return orderIdScreenOrderMap;
	}

	private Map<String, Agreement> buildDeviceIdAgreementMap(List<Agreement> agreementList) {
		Map<String, Agreement> deviceIdAgreementMap = Maps.newHashMap();
		for (Agreement agreement : agreementList) {
			if (agreement.getTbDeviceId() == null) {
				continue;
			}
			deviceIdAgreementMap.put(agreement.getTbDeviceId(), agreement);
		}
		return deviceIdAgreementMap;
	}

	/*
	 * METHODS
	 */

	private List<ScreenDevicePrintSettlement> buildScreenDevicePrintSettlementList(DateTime now) {
		List<ScreenDevicePrintSettlement> screenDevicePrintSettlementList = Lists.newArrayList();
		List<Map<String, Object>> devicePrintSettlementMapList = heatsMapper.scDevicePrintSettlementList(//
				now.minusYears(1).toString(DatePattern.YYYY_MM_DD.val())//
				, now.toString(DatePattern.YYYY_MM_DD.val())//
		);
		for (Map<String, Object> rs : devicePrintSettlementMapList) {
			try {
				ScreenDevicePrintSettlement s = new ScreenDevicePrintSettlement();
				s.setId(rs.get("id").toString());
				double blackMoney = 0d, colorMoney = 0d;
				if (rs.get("blackMoney") != null) {
					blackMoney = new Double(rs.get("blackMoney").toString());
				}
				if (rs.get("colorMoney") != null) {
					colorMoney = new Double(rs.get("colorMoney").toString());
				}
				s.setDeviceSerialNum(rs.get("deviceSerialNum") == null ? "" : rs.get("deviceSerialNum").toString());
				s.setBlackPrintUse((Integer) (rs.get("blackPrintUse") == null ? 0 : rs.get("blackPrintUse")));
				s.setColorPrintUse((Integer) (rs.get("colorPrintUse") == null ? 0 : rs.get("colorPrintUse")));
				s.setBlackMoney(blackMoney);
				s.setColorMoney(colorMoney);
				s.setDataData(rs.get("dataDate") == null ? "" : rs.get("dataDate").toString());
				s.setPushTime(rs.get("pushTime") == null ? "" : rs.get("pushTime").toString());
				screenDevicePrintSettlementList.add(s);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
				continue;
			}
		}
		return screenDevicePrintSettlementList;
	}

	private List<ScreenPackagePrint> buildScreenPackagePrintingList() {
		List<ScreenPackagePrint> screenPackagePrintingList = Lists.newArrayList();
		List<Map<String, Object>> packagePrintMapList = heatsMapper.scPackagePrintingList();
		for (Map<String, Object> rs : packagePrintMapList) {
			if (rs.get("tbDeviceId") == null) {
				continue;
			}
			double price = rs.get("price") == null ? 0 : (double) rs.get("price");
			screenPackagePrintingList.add(new ScreenPackagePrint(rs.get("id").toString()//
					, rs.get("tbDeviceId").toString()//
					, price//
			));
		}
		return screenPackagePrintingList;
	}

	/**
	 * 分析dataV_geo_atlas数据到数据表
	 */
	public void initAtlas() {
		logger.info("{} >>>> init atlas ", taskName);
		Stopwatch wt = Stopwatch.createStarted();
		String[] urls = new String[] { "province.json", "city.json", "district.json" };
		for (String url : urls) {
			try {
				String content = IOUtils
						.read(new InputStreamReader(new URL("http://localhost/_test/geo/" + url).openStream()));
				ObjectMapper om = new ObjectMapper();
				GeoJson cityJson = om.readValue(content, GeoJson.class);
				List<Row> rows = cityJson.getRows();
				for (Row row : rows) {
					int flag = heatsMapper.insertAtlas(row.getName(), row.getLevel(), row.getParent(),
							row.getPeople_count_2010(), row.getAdcode(), row.getLng(), row.getLat());
					logger.debug("{} insert compeled flag [{}]", taskName, flag);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("{} <<<< init atlas compeleted! {}", taskName, wt);
	}

	/**
	 * 建立 系统省市与 阿里省市 对应关系
	 * 
	 * @param sysProvinceAtlasMap
	 * @param sysCityAtlasMap
	 */
	public void buildAtlasMap(Map<String, GeoAtlas> sysProvinceAtlasMap, Map<String, GeoAtlas> sysCityAtlasMap) {
		logger.info("{} >>>> init atlas ", taskName);
		Stopwatch wt = Stopwatch.createStarted();
		// province
		Map<String, SysProvinces> provinceMap = mapper.scProvinceList().stream()
				.collect(Collectors.toMap(SysProvinces::getProvinces, Function.identity(), (o, n) -> n));
		List<GeoAtlas> geoAtlasList = heatsMapper.scGeoAtlasList();
		Map<String, List<GeoAtlas>> levelAtlasListMap = geoAtlasList.stream()
				.collect(Collectors.groupingBy(GeoAtlas::getLevel));
		List<GeoAtlas> provinceAtlasList = levelAtlasListMap.get(GeoAtlas.Level.province.name());
		Map<String, GeoAtlas> provinceAtlasMap = Maps.newHashMap();
		// provinceAtlasList.stream().collect(Collectors.toMap(GeoAtlas::getName,
		// Function.identity(), (o, n) -> n));
		for (GeoAtlas province : provinceAtlasList) {
			if (province == null) {
				continue;
			}
			String key = province.getName();
			if (key == null) {
				key = "";
			}
			provinceAtlasMap.put(key, province);
		}

		buildAdcodeLngLat(provinceMap, provinceAtlasMap, sysProvinceAtlasMap);
		// city
		Map<String, SysCity> cityMap = mapper.scCityList().stream()
				.collect(Collectors.toMap(SysCity::getCity, Function.identity(), (o, n) -> n));
		List<GeoAtlas> cityAtlasList = levelAtlasListMap.get(GeoAtlas.Level.city.name());
		Map<String, GeoAtlas> cityAtlasMap = cityAtlasList.stream()
				.collect(Collectors.toMap(GeoAtlas::getName, Function.identity(), (o, n) -> n));
		// int able = 0, unable = 0;
		for (String city : cityMap.keySet()) {
			boolean flag = false;// 可以处理标志
			String cityMapKey = null;
			String cityTrim = city.replaceAll("地区", "").replaceAll("族自治州", "").replaceAll("县", "").replaceAll("市", "");
			for (String cityAtlas : cityAtlasMap.keySet()) {
				if (city.equals(cityAtlas)) {
					// System.out.println("= 相同:\t" + city);
					flag = true;
					cityMapKey = city;
				} else if (cityAtlas.contains(cityTrim//
				)) {
					// System.out.println("< 包含:" + city + "\t" + cityAtlas);
					cityMapKey = cityAtlas;
					flag = true;
				} else if (city.contains(cityAtlas)) {
					// System.out.println("> 包含:" + city + "\t" + cityAtlas);
					cityMapKey = cityAtlas;
					flag = true;
				}
				if (city.contains("襄樊")) {
					cityMapKey = "襄阳市";
					flag = true;
				}
			}
			if (!flag) {
				logger.warn("{} - 无法处理:\t" + city, taskName);
			}
			// unable++;
			// } else {
			// able++;
			// }
			GeoAtlas atlas = cityAtlasMap.get(cityMapKey);
			sysCityAtlasMap.put(city, atlas);
		}
		// System.out.println("可处理:" + able + "不可处理:" + unable);

		// area district country
		// Map<String, SysAreaCounty> areaMap = mapper.scAreaList().stream()
		// .collect(Collectors.toMap(SysAreaCounty::getAreaCounty,
		// Function.identity(), (o, n) -> n));
		// List<GeoAtlas> districtAtlasList =
		// levelAtlasListMap.get(GeoAtlas.Level.province.name());

		logger.info("<<<< compeleted! compare and save geo {}", wt);
	}

	private void buildAdcodeLngLat(Map<String, SysProvinces> provinceMap, Map<String, GeoAtlas> provinceAtlasMap,
			Map<String, GeoAtlas> sysProvinceAtlasMap) {
		for (String province : provinceMap.keySet()) {
			String provinceMapKey = buildAtlasKey(provinceAtlasMap, province);
			GeoAtlas atlas = provinceAtlasMap.get(provinceMapKey);
			sysProvinceAtlasMap.put(province, atlas);
		}
	}

	private String buildAtlasKey(Map<String, GeoAtlas> provinceAtlasMap, String province) {
		boolean flag = false;// 可以处理标志
		String provinceMapKey = null;
		for (String provinceAtlas : provinceAtlasMap.keySet()) {
			if (province.equals(provinceAtlas)) {
				// System.out.println("= 相同:\t" + province);
				flag = true;
				provinceMapKey = province;
			} else if (provinceAtlas.contains(province)) {
				// System.out.println("< 包含:" + province + "\t" +
				// provinceAtlas);
				provinceMapKey = provinceAtlas;
				flag = true;
			} else if (province.contains(provinceAtlas)) {
				// System.out.println("> 包含:" + province + "\t" +
				// provinceAtlas);
				provinceMapKey = provinceAtlas;
				flag = true;
			}
		}
		if (!flag) {
			// System.out.println("- 无法处理:\t" + province);
		}
		return provinceMapKey;
	}

	public static void main(String[] args) throws UnknownHostException {
		ScreenStarter.main(args);
		// new ScreenHeatsHandler().init();
		// new ScreenDataHandler();
		new ScreenHeatsHandler().initAtlas();
	}
}
