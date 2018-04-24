package com.smartoa.service.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.smartoa.service.model.EngineerProcess;
import com.smartoa.service.model.GeoAtlas;
import com.smartoa.service.model.WorkOrderOpr;
import com.smartoa.service.model.screen.other.ScreenUserBill;

public interface ScreenHeatsMapper {
	List<Map<String, Object>> scPrintDaily(@Param("year1") String year1//
			, @Param("startTime") Date startTime//
			, @Param("endTime") Date endTime//
			, @Param("year2") String year2//
	);

	List<Map<String, Object>> scPackagePrintingList();

	List<Map<String, Object>> scDevicePrintSettlementList(//
			@Param("startTime") String startTime//
			, @Param("endTime") String endTime//
	);

	int insertAtlas(@Param("name") String name//
			, @Param("level") String level//
			, @Param("parent") String parent//
			, @Param("peopleCount2010") long peopleCount//
			, @Param("adcode") String adcode//
			, @Param("lng") float lng//
			, @Param("lat") float lat//
	);

	List<GeoAtlas> scGeoAtlasList();

	List<EngineerProcess> scEngineerProcessList(@Param("startTime") String startTime, @Param("endTime") String endTime);

	List<WorkOrderOpr> scWorkOrderOprList(@Param("timeStart") String startTime, @Param("timeEnd") String endTime);

	/**
	 * 查询时间范围内的用户订单信息
	 */
	List<ScreenUserBill> scUserBill(@Param("start") Date start, @Param("end") Date end);
}
