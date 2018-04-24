package com.smartoa.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ProviderApplication
 *
 * @Author zl
 * @Date 2017/5/12
 */
@SpringBootApplication
// 启注解事务管理
@EnableTransactionManagement
public class ScreenStarter extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {
	private static final Logger logger = LoggerFactory.getLogger(ScreenStarter.class);
	public static ApplicationContext applicationContext;

	@Override
	public void customize(ConfigurableEmbeddedServletContainer configurableEmbeddedServletContainer) {
	}

	public static void main(String[] args) throws UnknownHostException {
		SpringApplication application = new SpringApplication(ScreenStarter.class);
		application.setBannerMode(Mode.OFF);
		applicationContext = application.run(args);
		Environment env = applicationContext.getEnvironment();
		String port = env.getProperty("server.port", "8088");
		logger.info("Access URLS:\n--------------------------------------------\n\t"
				+ "Local: \t http://127.0.0.1:{}\n\t" + "External: \thttp://{}:{}", port,
				InetAddress.getLocalHost().getHostAddress(), port);
	}
}
