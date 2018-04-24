package com.smartoa.service.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.smartoa.service.model.ts.TsProfitLoss;

public interface TsProfitLossMapper {
	List<Map<String, Object>> tsSerialNum2ModeList();

	List<TsProfitLoss> tsProfitLossDataList(@Param("startDate") Date startDate);

	List<Map<String, Object>> tsProfitLossDepreciationList(@Param("month") String month);

	List<Map<String, Object>> tsProfitLossBillingDetail(@Param("month") String month);

	List<Map<String, Object>> tsProfitLossPartsConsumables(@Param("month") String month);

	/**
	 * @param month
	 *            yyyy-MM
	 * @return
	 */
	List<Map<String, Object>> tsProfitLossRecommended(@Param("month") String month);
}
