package com.smartoa.service.impl.screen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.smartoa.service.api.IProfitLossJobService;
import com.smartoa.service.mapper.TsProfitLossMapper;
import com.smartoa.service.model.ts.TsProfitLoss;
import com.smartoa.service.utils.JobLogUtils;

import jersey.repackaged.com.google.common.collect.Maps;

@Service("profitLossJobService")
public class ProfitLossJobService implements IProfitLossJobService {
	JobLogUtils log = new JobLogUtils(ProfitLossJobService.class);
	@Autowired
	TsProfitLossMapper mapper;
	String dtMonthFormat = "yyyy-MM";
	String dtDayFormat = "yyyy-MM-dd";

	@Override
	public void run(String jobName) {
		Set<String> partsClasses = new HashSet<String>();
		Set<String> consumablesClasses = new HashSet<String>();
		
		log.setJobName(jobName);
		DateTime statisticMonth = DateTimeFormat.forPattern(dtMonthFormat).parseDateTime("2018-03");
		String month = statisticMonth.toString(dtMonthFormat);

		Stopwatch wt = Stopwatch.createStarted();
		log.info(">>>> 开始计算{}月的损益: ts_profit_loss_* ", month);
		
		/* 设备名称, 租金, 超印 */
		List<TsProfitLoss> data = mapper.tsProfitLossDataList(statisticMonth.minusMonths(3).toDate());
		log.info("本月产品项数量:{}", data.size());
		// 计算 是否为起租月, 本期月份数 ,月租金
		buildIfStartLeaseRentMonthsItemMonthRent(month, data);
		// 计算月抵扣情况
		buildItemMonthCoupon(data);

		/* 折旧 */
		List<Map<String, Object>> depreList = mapper.tsProfitLossDepreciationList(month);
		buildDepreciationFee(data, depreList);
		log.info("折旧数量:{}", depreList.size());

		/* 安装 , 服务 费 */
		List<Map<String, Object>> billingDetailList = mapper.tsProfitLossBillingDetail(month);
		buildInstallServiceFee(month, data, billingDetailList);
		log.info("结算信息量:{}", billingDetailList.size());

		/* 耗材 */
		List<Map<String, Object>> partsList = buildPartsConsumablesFee(month, data);
		log.info("耗材使用量:{}", partsList.size());
		
		/* 推荐 */
		List<Map<String, Object>> recommendList = mapper.tsProfitLossRecommended(month);
		log.info("推荐订单数:{}", recommendList.size());
		//
		log.info(">>>> 计算结束 {}", wt);
	}

	private List<Map<String, Object>> buildPartsConsumablesFee(String month, List<TsProfitLoss> data) {
		List<Map<String, Object>> partsList = mapper.tsProfitLossPartsConsumables(month);
		Map<String, List<Map<String, Object>>> deviceParts = Maps.newHashMap();
		for (Map<String, Object> rs : partsList) {
			String deviceId = rs.get("deviceId") == null ? null : rs.get("deviceId").toString();
			if (deviceId == null) {
				continue;
			}
			List<Map<String, Object>> lst = deviceParts.get(deviceId);
			if (lst == null) {
				lst = Lists.newArrayList();
				deviceParts.put(deviceId, lst);
			}
			lst.add(rs);
		}
		//
		for (TsProfitLoss bean : data) {
			List<Map<String, Object>> partsFeeMap = deviceParts.get(bean.getDeviceId());
			if (CollectionUtils.isEmpty(partsFeeMap)) {
				continue;
			}
			BigDecimal partsFee = new BigDecimal(0);
			BigDecimal consumablesFee = new BigDecimal(0);
			for (Map<String, Object> rs : partsFeeMap) {
				String classId = rs.get("classId") == null ? null : rs.get("classId").toString();
				BigDecimal productPrice = rs.get("productPrice") == null ? null : (BigDecimal) rs.get("productPrice");
				// XXX
			}
		}
		return partsList;
	}

	private void buildInstallServiceFee(String month, List<TsProfitLoss> data,
			List<Map<String, Object>> billingDetailList) {
		Map<String, Map<String, Object>> modelBillingDetailMap = Maps.newHashMap();
		for (Map<String, Object> rs : billingDetailList) {
			String model = rs.get("model") == null ? null : rs.get("model").toString();
			modelBillingDetailMap.put(model, rs);
		}
		for (TsProfitLoss bean : data) {
			try {
				String startLeaseMonth = new DateTime(bean.getStartLease()).toString(dtMonthFormat);
				Map<String, Object> fees = modelBillingDetailMap.get(bean.getDeviceModel());
				if (fees == null) {
					continue;
				}
				BigDecimal installFee = fees.get("installPrice") == null ? null : (BigDecimal) fees.get("installPrice");
				BigDecimal serviceFee = fees.get("servicePrice") == null ? null : (BigDecimal) fees.get("servicePrice");
				if (month.equals(startLeaseMonth)) { // 起租月为统计月时, 则无服务费, 仅收安装 费
					bean.setInstallFee(installFee);
				} else {
					bean.setServiceFee(serviceFee);
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}

	private void buildDepreciationFee(List<TsProfitLoss> data, List<Map<String, Object>> depreList) {
		Map<String, BigDecimal> deviceIdDepreMap = Maps.newHashMap();
		for (Map<String, Object> rs : depreList) {
			String deviceId = rs.get("deviceId") == null ? null : rs.get("deviceId").toString();
			if (deviceId == null) {
				continue;
			}
			BigDecimal provisionMoney = rs.get("provisionMoney") == null ? null : (BigDecimal) rs.get("provisionMoney");
			deviceIdDepreMap.put(deviceId, provisionMoney);
		}
		for (TsProfitLoss bean : data) {
			bean.setProvisionMoney(deviceIdDepreMap.get(bean.getDeviceId()));
		}
	}

	private void buildItemMonthCoupon(List<TsProfitLoss> data) {
		// 分组统计每个账单下的并计算出来账单的总租金金额
		// 分组完, 计算每个单品应该得到的抵扣; 使用计算占比 , 从而计算出来相应的抵扣金额. 保留两位小数,四舍五入
		Map<String, List<TsProfitLoss>> billSnGroupMap = new HashMap<String, List<TsProfitLoss>>();
		for (TsProfitLoss bean : data) {
			String bSn = bean.getBillSn();
			List<TsProfitLoss> list = billSnGroupMap.get(bSn);
			if (list == null) {
				list = new ArrayList<TsProfitLoss>();
				billSnGroupMap.put(bSn, list);
			}
			list.add(bean);
		}
		//
		for (String billSn : billSnGroupMap.keySet()) {
			List<TsProfitLoss> list = billSnGroupMap.get(billSn);
			BigDecimal billTotalRent = new BigDecimal(0);
			for (TsProfitLoss bean : list) {
				billTotalRent.add(bean.getTotalAmount());
			}
			for (TsProfitLoss bean : list) {
				try {
					bean.setBillTotalRent(billTotalRent);
					// couponPaidAmount * (totalAmount/billTotalRent)/ rentMonths
					BigDecimal itemMonthCoupon = bean.getCouponPaidAmount()
							.multiply((bean.getTotalAmount().divide(bean.getBillTotalRent())))
							.divide(new BigDecimal(bean.getRentMonths()));
					BigDecimal itemMonthOrderCoupon = bean.getOrderCouponDiscount()
							.multiply((bean.getTotalAmount().divide(bean.getBillTotalRent())))
							.divide(new BigDecimal(bean.getRentMonths()));
					// itemMonthRent - itemMonthCoupon - itemMonthOrderCoupon
					BigDecimal itemMonthAccountingRent = bean.getItemMonthRent().subtract(bean.getItemMonthCoupon())
							.subtract(bean.getItemMonthOrderCoupon());
					bean.setItemMonthCoupon(itemMonthCoupon);
					bean.setItemMonthOrderCoupon(itemMonthOrderCoupon);
					bean.setItemMonthAccountingRent(itemMonthAccountingRent);
				} catch (Exception e) {
					log.warn(e);
				}
			}
		}
	}

	private void buildIfStartLeaseRentMonthsItemMonthRent(String month, List<TsProfitLoss> data) {
		for (TsProfitLoss bean : data) {
			try {// 计算本账单租的月份数
				DateTime start = new DateTime(bean.getStartDate());
				DateTime end = new DateTime(bean.getEndDate());
				Period p = new Period(start, end, PeriodType.months());
				int months = p.getMonths() + 1;
				bean.setRentMonths(months);

			} catch (Exception e) {
				log.warn(e);
			}
			try {// 月租金
					// bean.setItemMonthRent(bean.getTotalAmount().divide(new
					// BigDecimal(bean.getRentMonths())));
			} catch (Exception e) {
				log.warn(e);
			}
			try {// 计算是否为起租月
				if (new DateTime(bean.getStartLease()).toString(dtMonthFormat).equals(month)) {
					bean.setIfStartMonth(true);
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
	}

	private Map<String, String> buildDeviceSn2ModelMap() {
		Map<String, String> deviceSn2ModelMap = Maps.newHashMap();
		List<Map<String, Object>> list = mapper.tsSerialNum2ModeList();
		for (Map<String, Object> rs : list) {
			Object sn = rs.get("deviceSerialNum");
			Object model = rs.get("deviceModelDictCode");
			if (sn == null) {
				continue;
			}
			deviceSn2ModelMap.put(sn.toString(), model == null ? null : model.toString());
		}
		return deviceSn2ModelMap;
	}

}
