package spider.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobLogUtils {
	private static Logger log = null;
	String jobName;
	boolean openLog = true;// 默认日志是开启着的 , info, debug级别日志的控制器

	public JobLogUtils setJobName(String jobName) {
		if (StringUtils.isNotEmpty(jobName)) {
			this.jobName = "【" + jobName + "】";
		}
		return this;
	}

	public String getJobName() {
		return this.jobName;
	}

	public JobLogUtils colseLog() {
		this.openLog = false;
		return this;
	}

	public JobLogUtils openLog() {
		this.openLog = true;
		return this;
	}

	public JobLogUtils(String jobName, Class<?> clz) {
		log = LoggerFactory.getLogger(clz);
		this.jobName = "【" + jobName + "】";
	}

	public JobLogUtils(Class<?> clz) {
		log = LoggerFactory.getLogger(clz);
		this.jobName = "【" + clz.getName() + "】 ";
	}

	public void info(String format) {
		if (!openLog) {
			return;
		}
		log.info(jobName + format);
	}

	public void info(String format, Object... arguments) {
		if (!openLog) {
			return;
		}
		log.info(jobName + format, arguments);
	}

	public void debug(String format) {
		if (!openLog) {
			return;
		}
		log.debug(jobName + format);
	}

	public void debug(String format, Object... arguments) {
		if (!openLog) {
			return;
		}
		log.debug(jobName + format, arguments);
	}

	public void warn(String format) {
		log.warn(jobName + format);
	}

	public void warn(Throwable e) {
		log.warn(jobName, e);
	}

	public void warn(String format, Object... arguments) {
		log.warn(jobName + format, arguments);
	}

	public void error(String msg, Throwable t) {
		log.error(jobName + msg, t);
	}

	public void error(String msg) {
		log.error(jobName + msg);
	}

	public void error(String format, Object... arg) {
		log.error(jobName + format, arg);
	}

}
