package com.smartoa.service.impl.screen.quartz;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.smartoa.service.api.IProfitLossJobService;
import com.smartoa.service.impl.screen.Job;
import com.smartoa.service.impl.screen.handler.ScreenAssetsHandler;
import com.smartoa.service.impl.screen.handler.ScreenDataHandlerV2;
import com.smartoa.service.impl.screen.handler.ScreenHeatsHandler;
import com.smartoa.service.impl.screen.handler.ScreenScheduleHandler;
import com.smartoa.service.utils.JobLogUtils;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableScheduling // 启用定时任务
@Slf4j
public class ScreenQuartz {
	private static volatile boolean isRunning = false;
	JobLogUtils logger = new JobLogUtils("损益计算", ScreenQuartz.class);

	@Autowired
	IProfitLossJobService profitLossJobService;
	@Autowired
	Job job;

	@Scheduled(fixedDelay = 1000 * 3, initialDelay = 0)
	public void profitAssets() {
		profitLossJobService.run(logger.getJobName());
		// job.run();
		logger.info("执行完毕");
	}

	/**
	 * 分时日月周<br/>
	 * 秒分时日月星期年
	 * 
	 * @see <a href=
	 *      "http://blog.csdn.net/u013758116/article/details/53157152">http://blog.csdn.net/u013758116/article/details/53157152</a>
	 */
	// @Scheduled(fixedDelay = 1000 * 60 * 15, initialDelay = 1000 * 20)
	public void scheduler() {
		if (isRunning == true) {
			return;
		}
		isRunning = true;
		log.info(">>>> scheduler start... {}", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
		try {
			new ScreenAssetsHandler().init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new ScreenDataHandlerV2().init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new ScreenHeatsHandler().init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			new ScreenScheduleHandler().init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		isRunning = false;
		log.info("<<<< scheduler compeleted! {}", new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
	}

	public boolean isRunning() {
		return isRunning;
	}
}
