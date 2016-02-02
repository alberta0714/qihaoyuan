package com.alberta0714.qihaoyuan;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alberta0714.common.Constant;
import com.google.common.base.Stopwatch;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

public class Listener implements ServletContextListener {
	private static final Logger logger = LoggerFactory.getLogger(Listener.class);
	Stopwatch wt = Stopwatch.createUnstarted();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			logger.info("IP:{}", ip);
		} catch (UnknownHostException e1) {
			logger.error("", e1);
		}
		logger.info("");

		logger.info(">>>> application start");
		wt.reset().start();
		if (!Constant.BASEDIR.exists()) {
			if (!Constant.BASEDIR.mkdirs()) {
				throw new RuntimeException("base dir create failed! " + Constant.BASEDIR.getAbsolutePath());
			}
		} else if (Constant.BASEDIR.isFile()) {
			throw new RuntimeException("base dir is not a directory " + Constant.BASEDIR.getAbsolutePath());
		}
		// Create your Configuration instance, and specify if up to what
		// FreeMarker
		// version (here 2.3.22) do you want to apply the fixes that are not
		// 100%
		// backward-compatible. See the Configuration JavaDoc for details.
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
		// Specify the source where the template files come from. Here I set a
		// plain directory for it, but non-file-system sources are possible too:
		File templatesDir = new File(Constant.BASEDIR, "templates");
		if (!templatesDir.exists()) {
			templatesDir.mkdirs();
		}
		try {
			cfg.setDirectoryForTemplateLoading(templatesDir);
		} catch (IOException e) {
			logger.error("", e);
		}
		// Set the preferred charset template files are stored in. UTF-8 is
		// a good choice in most applications:
		cfg.setDefaultEncoding("UTF-8");
		// Sets how errors will appear.
		// During web page *development*
		// TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
		// cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("<<<< application destroyed {}", wt.stop());
	}

}
