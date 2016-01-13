package com.alberta0714.qihaoyuan;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.Constant;
import com.google.common.base.Stopwatch;

public class Listener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	Stopwatch wt = Stopwatch.createUnstarted();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info(">>>> application start");
		if (!Constant.BASEDIR.exists()) {
			if (!Constant.BASEDIR.mkdirs()) {
				throw new RuntimeException("base dir create failed! " + Constant.BASEDIR.getAbsolutePath());
			}
		} else if (Constant.BASEDIR.isFile()) {
			throw new RuntimeException("base dir is not a directory " + Constant.BASEDIR.getAbsolutePath());
		}
		wt.reset().start();

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("<<<< application destroyed {}", wt.stop());
	}

}
