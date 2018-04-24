package com.smartoa.service.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.smartoa.service.model.screen.other.ScreenEngineerTask;

public interface ScreenScheduleMapper {

	/*
	 * 反复执行,不断刷新工程师当前位置及,当前的任务状态
	 */
	List<ScreenEngineerTask> scEngineerTaskList(//
			@Param("willGoTimeStart") String start//
			, @Param("willGoTimeEnd") String end);

	List<Map<String, Object>> scWorkOrderList(@Param("ids") String ids);
	
	/**
	 * 获取服务享受时间定义
	 */
	Map<String, Object> scWorkTime();
}
