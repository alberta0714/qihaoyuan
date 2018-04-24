package com.smartoa.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class ScreenConfig {
	//
	@Value("${screen.op.data.id}")
	String screenOpDataId;
	@Value("${screen.op.data.token}")
	String screenOpDataToken;
	//
	@Value("${screen.op.assets.id}")
	String screenOpAssetsId;
	@Value("${screen.op.assets.token}")
	String screenOpAssetsToken;
	//
	@Value("${screen.op.heats.id}")
	String screenOpHeatsId;
	@Value("${screen.op.heats.token}")
	String screenOpHeatsToken;

	/*
	 * servicer
	 */
	//
	@Value("${screen.servicer.data.id}")
	String screenServicerDataId;
	@Value("${screen.servicer.data.token}")
	String screenServicerDataToken;
	//
	@Value("${screen.servicer.assets.id}")
	String screenServicerAssetsId;
	@Value("${screen.servicer.assets.token}")
	String screenServicerAssetsToken;
	//
	@Value("${screen.servicer.heats.id}")
	String screenServicerHeatsId;
	@Value("${screen.servicer.heats.token}")
	String screenServicerHeatsToken;
	//
	@Value("${screen.servicer.schedule.id}")
	String screenServicerScheduleId;
	@Value("${screen.servicer.schedule.token}")
	String screenServicerScheduleToken;
}
