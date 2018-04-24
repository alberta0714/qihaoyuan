package com.smartoa.service.impl.screen.handler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.smartoa.common.constant.DictionaryEnum;
import com.smartoa.service.ScreenStarter;
import com.smartoa.service.impl.screen.holder.ScreenDataHolder;
import com.smartoa.service.mapper.ScreenScheduleMapper;
import com.smartoa.service.model.Dictionary;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.screen.components.BasicPieChart;
import com.smartoa.service.model.screen.components.Dashboard;
import com.smartoa.service.model.screen.components.DigitalFlop;
import com.smartoa.service.model.screen.components.FlyLine;
import com.smartoa.service.model.screen.components.HorizontalBasicBarGraph;
import com.smartoa.service.model.screen.components.HorizontalCapsuleBarGraph;
import com.smartoa.service.model.screen.components.MultiText;
import com.smartoa.service.model.screen.other.ScOperation;
import com.smartoa.service.model.screen.other.ScheduleBean;
import com.smartoa.service.model.screen.other.ScreenEngineerTask;
import com.smartoa.service.model.screen.other.ScreenEngineerTaskEfficiency;
import com.smartoa.service.model.screen.other.ScreenEngineerTasksEfficiencyResult;

import jersey.repackaged.com.google.common.collect.Maps;

public class ScreenScheduleHandler extends ScreenDataHandlerV2 {
	private static final Logger logger = LoggerFactory.getLogger(ScreenScheduleHandler.class);
	ScreenScheduleMapper scheduleMapper = (ScreenScheduleMapper) ScreenStarter.applicationContext
			.getBean("screenScheduleMapper");

	public void init() {
		// X config settings
		DateTime dt = new DateTime();
		DateTime dtStart = dt// 开始时间
				.minusMonths(3)// XXX 正式上线时，需要删除此段代码
				.withTimeAtStartOfDay()//
		;
		DateTime dtEnd = dt; // 结束时间

		// X pre load
		Map<String, ScheduleBean> servicerMap = ScreenDataHolder.servicerScheduleMap;
		Stopwatch wt = Stopwatch.createStarted();
		logger.info(">>>> Screen schedule initializing...");
		// 工程师 服务商Id 当前位置 客户[经纬度] 任务状态
		List<ScreenEngineerTask> scEngineerTaskList = scheduleMapper.scEngineerTaskList(//
				dtStart.toString(DatePattern.YYYYMMDDHHMMSS.val())//
				, dtEnd.toString(DatePattern.YYYYMMDDHHMMSS.val()));
		logger.debug("sc engineer task list size:{}", scEngineerTaskList.size());
		// 当天任务状态 通过 运营数据接口分离出来此块数据内容
		Map<String, List<ScreenEngineerTask>> servicerIdListMap = scEngineerTaskList.stream()
				.collect(Collectors.groupingBy(ScreenEngineerTask::getServicerId));
		List<Dictionary> dicList = mapper.scDicList();
		Map<String, Map<String, Dictionary>> dicMap = buildDicMap(dicList);
		// 装载 工程师的签到信息 虚拟4为签到。 在派工状态下，有到达时间，即为签到
		for (ScreenEngineerTask task : scEngineerTaskList) {
			if (task.getIfFinished() == null) {
				continue;
			}
			if (task.getIfFinished().equals(3) && task.getArrivalTime() != null) {
				task.setIfFinished(4); // 伪状态码4，代表签到
			}
		}

		// X 计算#
		// XX 工程师飞线
		buildFlyLines(servicerMap, servicerIdListMap);
		// XX 今天截止到现在的服务任务件数
		buildTaskCount(servicerMap, servicerIdListMap);
		// XX 截止到昨天此时的服务任务件数
		buildTaskCountYesterday(dtStart, dtEnd, servicerMap, scEngineerTaskList);
		// XX patch servicerInfo 用于显示服务商的名称
		buildServicerName(servicerMap);
		// XX 任务性质饼图 任务性质分类 ，及内部进展统计
		Map<String, Dictionary> taskNatureDic = dicMap.get(DictionaryEnum.WORK_ORDER_CATEGORY.value());
		for (String servicerId : servicerIdListMap.keySet()) {
			List<ScreenEngineerTask> tasks = servicerIdListMap.get(servicerId);
			buildServicerTaskStatus(servicerId, tasks, servicerMap, taskNatureDic);
		}

		// XX 任务性质区域排名
		for (String servicerId : servicerIdListMap.keySet()) {
			List<ScreenEngineerTask> tasks = servicerIdListMap.get(servicerId);
			Map<String, List<ScreenEngineerTask>> areaListMap = tasks.stream()
					.filter(item -> item.getAreaCounty() != null)
					.collect(Collectors.groupingBy(ScreenEngineerTask::getAreaCounty));
			List<HorizontalBasicBarGraph> topCitiesOfTask = Lists.newArrayList(); // 任务数区域排名 Data
			for (String area : areaListMap.keySet()) {
				List<ScreenEngineerTask> lst = areaListMap.get(area);
				topCitiesOfTask.add(new HorizontalBasicBarGraph(area, Integer.toString(lst.size()), "1"));
			}
			// servicerIdListMap
			servicerMap.get(servicerId).setTopCitiesOfTask(topCitiesOfTask);
		}
		// 派工逾期、逾期率
		buildWorkOrderOverDue(servicerMap, scEngineerTaskList, servicerIdListMap);
		// 任务的前往、完工逾期及逾期率
		for (String servicerId : servicerIdListMap.keySet()) {
			List<HorizontalBasicBarGraph> goWarn = Lists.newArrayList(); // 前往逾期
			List<Dashboard> goDash = Lists.newArrayList();
			List<HorizontalBasicBarGraph> finishedWarn = Lists.newArrayList(); // 完工逾期
			List<Dashboard> finishedDash = Lists.newArrayList();// 派工，前往，完工 逾期率

			int goOds = 0;// 前往逾期计数
			int finishedOds = 0;// 完工逾期计数
			List<ScreenEngineerTask> tasks = servicerIdListMap.get(servicerId);
			for (ScreenEngineerTask task : tasks) {
				String seriesA = "0";
				String seriesB = "0";
				Integer planTimeOverDue = task.getPlanTimeOutLong() == null ? 0 : task.getPlanTimeOutLong();
				Integer planFinishedTimeOverDue = task.getPlanTimeOutLongEnd() == null ? 0
						: task.getPlanTimeOutLongEnd();
				if (planTimeOverDue > 15 && planTimeOverDue <= 30) {
					seriesA = "1";
					goOds++;
				} else if (planTimeOverDue > 30 && planTimeOverDue <= 180) {
					seriesA = "2";
					goOds++;
				} else if (planTimeOverDue > 180) {
					seriesA = "3";
					goOds++;
				}
				if (planFinishedTimeOverDue > 15 && planFinishedTimeOverDue <= 30) {
					seriesB = "1";
					finishedOds++;
				} else if (planFinishedTimeOverDue > 30 && planFinishedTimeOverDue <= 180) {
					seriesB = "2";
					finishedOds++;
				} else if (planFinishedTimeOverDue > 180) {
					seriesB = "3";
					finishedOds++;
				}
				goWarn.add(new HorizontalBasicBarGraph(task.getRealName(),
						planTimeOverDue == 0 ? "0.01" : Integer.toString(planTimeOverDue), seriesA));
				finishedWarn.add(new HorizontalBasicBarGraph(task.getRealName(),
						planFinishedTimeOverDue == 0 ? "0.01" : Integer.toString(planFinishedTimeOverDue), seriesB));
			}

			double rateA = 0d;
			double rateB = 0d;
			if (CollectionUtils.isNotEmpty(tasks)) {
				rateA = (double) goOds / tasks.size();
				rateB = (double) finishedOds / tasks.size();
			}
			goDash.add(new Dashboard("前往逾期率", new BigDecimal(rateA).setScale(2, RoundingMode.HALF_UP).doubleValue()));
			finishedDash
					.add(new Dashboard("完工逾期率", new BigDecimal(rateB).setScale(2, RoundingMode.HALF_UP).doubleValue()));

			ScheduleBean bean = servicerMap.get(servicerId);
			if (bean == null) {
				bean = new ScheduleBean();
				servicerMap.put(servicerId, bean);
			}
			bean.setGoWarn(goWarn);
			bean.setGoDash(goDash);
			bean.setFinishedWarn(finishedWarn);
			bean.setFinishedDash(finishedDash);
		}
		// 工程师效率，统计出来每个工程师每段时间执行的任务；
		buildEngineerEfficiency(servicerMap, scEngineerTaskList, false);
		buildEngineerEfficiency(servicerMap, scEngineerTaskList, true);
		logger.info("<<<< Initializing screen schedule  compeleted! {}", wt);
	}

	private void buildEngineerEfficiency(Map<String, ScheduleBean> servicerMap,
			List<ScreenEngineerTask> scEngineerTaskList, boolean ifToday) {
		Map<String, List<ScreenEngineerTask>> servicerIdListMap = scEngineerTaskList.stream()
				.collect(Collectors.groupingBy(ScreenEngineerTask::getServicerId));

		DateTime now = new DateTime();
		DateTime dayStart = now.withTimeAtStartOfDay();
		// DateTime dayEnd = now.plusDays(1).withTimeAtStartOfDay();
		Map<String, Object> wkTimeDb = scheduleMapper.scWorkTime();
		DateTime wkTimeStart = null; // 当天任务的开始时间
		DateTime wkTimeEnd = null;// 当天任务的结束时间
		String today = now.toString("yyyy/MM/dd");
		wkTimeStart = DateTime.parse(today + " " + wkTimeDb.get("worktimeStart"),
				DateTimeFormat.forPattern("yyyy/MM/dd HH:mm"));
		wkTimeEnd = DateTime.parse(today + " " + wkTimeDb.get("worktimeEndPm"),
				DateTimeFormat.forPattern("yyyy/MM/dd HH:mm"));

		Map<String, List<Map<String, ScreenEngineerTaskEfficiency>>> servicerIdOprs = Maps.newHashMap();
		for (String servicerId : servicerIdListMap.keySet()) {
			// 服务商下的工程师的效率列表
			List<Map<String, ScreenEngineerTaskEfficiency>> oprList = servicerIdOprs.get(servicerId);
			if (oprList == null) {
				oprList = Lists.newArrayList();
				servicerIdOprs.put(servicerId, oprList);
			}
			List<ScreenEngineerTask> tasks = servicerIdListMap.get(servicerId);// 原始数据
			Map<String, ScreenEngineerTaskEfficiency> engineerIdTaskMap = Maps.newHashMap();
			for (ScreenEngineerTask task : tasks) {
				ScreenEngineerTaskEfficiency ef = engineerIdTaskMap.get(task.getEngineerId());
				List<ScOperation> taskList = null;
				if (ef == null) {
					ef = new ScreenEngineerTaskEfficiency();
					ef.setEngineerId(task.getEngineerId());
					ef.setEngineerName(task.getRealName());
					ef.setLevel(task.getEngineerLevel());
					ef.setLevelName(task.getLevelName());
					taskList = Lists.newArrayList();
					ef.setList(taskList);
					engineerIdTaskMap.put(task.getEngineerId(), ef);
				}
				taskList = ef.getList();
				ScOperation t = new ScOperation();
				t.setStart(task.getWillGoTime());// willgoTime
				t.setEnd(task.getFinishedTime());
				t.setPlanStart(task.getPlanTime());// planTime
				t.setPlanEnd(task.getPlanTimeEnd());
				t.setDurationMinutes(buildDateDiffMinutes(t.getStart(), t.getEnd()));
				// t.setPreDifMinutes(preDifMinutes); // 与上一次任务距离 的时间；
				t.setId(task.getId());
				t.setIfFinished(task.getIfFinished());
				t.setNatures(task.getTaskNature() == null ? null : Integer.toString(task.getTaskNature()));
				t.setCustId(task.getCustId());
				t.setCustName(task.getCustName());
				// t.setDeviceId(deviceId);
				t.setDeviceModel(task.getDeviceModels());

				// XXX 只要当天的数据
				if (ifToday && t.getPlanStart().getTime() < dayStart.getMillis()) {
					continue;
				}
				taskList.add(t);
			}

			oprList.add(engineerIdTaskMap);
			ScheduleBean bean = servicerMap.get(servicerId);
			ScreenEngineerTasksEfficiencyResult efr = new ScreenEngineerTasksEfficiencyResult();
			if (bean == null) {
				bean = new ScheduleBean();
				servicerMap.put(servicerId, bean);
			}
			List<ScreenEngineerTaskEfficiency> engineerEfficiency = Lists.newArrayList();
			Date efStart = null;
			Date efEnd = null;
			for (String engineerId : engineerIdTaskMap.keySet()) {
				ScreenEngineerTaskEfficiency ef = engineerIdTaskMap.get(engineerId);
				if (ef == null || CollectionUtils.isEmpty(ef.getList())) {
					continue;
				}
				Collections.sort(ef.getList(), new Comparator<ScOperation>() {
					@Override
					public int compare(ScOperation o1, ScOperation o2) {
						if (o1 == null || o2 == null || o1.getStart() == null || o2.getStart() == null) {
							return 0;
						}
						return (int) (o1.getStart().getTime() - o2.getStart().getTime());
					}
				});
				if (CollectionUtils.isNotEmpty(ef.getList())) {
					ef.setStart(ef.getList().get(0).getStart());
					// build end
					Date end = ef.getList().get(ef.getList().size() - 1).getEnd();
					if (ef.getList().get(ef.getList().size() - 1).getPlanEnd() != null
							&& ef.getList().get(ef.getList().size() - 1).getPlanEnd().getTime() > end.getTime()) {
						end = ef.getList().get(ef.getList().size() - 1).getPlanEnd();
					}
					ef.setEnd(end);
				}
				if (ef.getStart() != null && efStart == null) {
					efStart = ef.getStart();
				}
				if (ef.getEnd() != null && efEnd == null) {
					efEnd = ef.getEnd();
				}
				if (ef.getStart() != null && efStart != null && ef.getStart().getTime() < efStart.getTime()) {
					efStart = ef.getStart();
				}
				if (ef.getEnd() != null && efEnd != null && ef.getEnd().getTime() > efEnd.getTime()) {
					efEnd = ef.getEnd();
				}
				engineerEfficiency.add(ef);
			}
			if (ifToday) {
				efr.setStart(wkTimeStart.minusMinutes(30).toDate());
				efr.setEnd(wkTimeEnd.plusHours(1).toDate());
			} else {
				efr.setStart(efStart);
				efr.setEnd(efEnd);
			}
			efr.setTasks(engineerEfficiency);
			if (ifToday) {
				bean.setEngineerTasksEfficiencyResultToday(efr);
			} else {
				bean.setEngineerTasksEfficiencyResult(efr);
			}
		}
	}

	private Integer buildDateDiffMinutes(Date a, Date b) {
		if (a == null || b == null) {
			return null;
		}
		int difMinutes = (int) (b.getTime() - a.getTime()) / 1000 / 60;
		return difMinutes;
	}

	private void buildWorkOrderOverDue(Map<String, ScheduleBean> servicerMap,
			List<ScreenEngineerTask> scEngineerTaskList, Map<String, List<ScreenEngineerTask>> servicerIdListMap) {
		Set<String> workOrderIds = Sets.newHashSet();
		for (ScreenEngineerTask se : scEngineerTaskList) {
			workOrderIds.add(se.getWorkOrderId());
		}
		String woIds = "'" + StringUtils.join(workOrderIds, "','") + "'";
		List<Map<String, Object>> workOrders = scheduleMapper.scWorkOrderList(woIds);
		Map<String, List<Map<String, Object>>> sIdWorkOrders = buildServicerIdWorkOrdersMap(workOrders);
		for (String servicerId : servicerIdListMap.keySet()) {
			List<HorizontalBasicBarGraph> sendWarn = Lists.newArrayList(); // 派工逾期
			List<Dashboard> sendDash = Lists.newArrayList();// 派工逾期率
			int count = 0;// 逾期的数量
			List<Map<String, Object>> workOrderLst = sIdWorkOrders.get(servicerId);
			for (Map<String, Object> wo : workOrderLst) {
				String series = "0";
				String custName = wo.get("custName") == null ? null : wo.get("custName").toString();
				Integer timeOutLong = wo.get("timeOutLong") == null ? null : (Integer) wo.get("timeOutLong");
				if (timeOutLong != null && timeOutLong > 30 && timeOutLong <= 60) {
					series = "1";
					count++;
				} else if (timeOutLong != null && timeOutLong > 60 && timeOutLong <= 180) {
					series = "2";
					count++;
				} else if (timeOutLong != null && timeOutLong > 180) {
					series = "3";
					count++;
				}
				sendWarn.add(new HorizontalBasicBarGraph(custName,
						timeOutLong == null ? "0.01" : Integer.toString(timeOutLong), series));
			}
			double rate = 0d;
			if (workOrderLst != null && workOrderLst.size() > 0) {
				rate = (double) count / workOrderLst.size();
			}
			sendDash.add(new Dashboard("派工逾期率", new BigDecimal(rate).setScale(2, RoundingMode.HALF_UP).doubleValue()));
			servicerMap.get(servicerId).setSendWarn(sendWarn);
			servicerMap.get(servicerId).setSendDash(sendDash);
		}
	}

	private Map<String, List<Map<String, Object>>> buildServicerIdWorkOrdersMap(List<Map<String, Object>> workOrders) {
		Map<String, List<Map<String, Object>>> sIdWorkOrders = Maps.newHashMap();
		for (Map<String, Object> rs : workOrders) {
			// String custName = rs.get("custName") == null ? null :
			// rs.get("custName").toString();
			// Integer timeOutLong = rs.get("timeOutLong") == null ? null :
			// (Integer)rs.get("timeOutLong");
			String servicerId = rs.get("servicerId") == null ? null : rs.get("servicerId").toString();
			if (StringUtils.isEmpty(servicerId)) {
				continue;
			}
			List<Map<String, Object>> wos = sIdWorkOrders.get(servicerId);
			if (wos == null) {
				wos = Lists.newArrayList();
				sIdWorkOrders.put(servicerId, wos);
			}
			wos.add(rs);
		}
		return sIdWorkOrders;
	}

	private void buildServicerTaskStatus(String servicerId, List<ScreenEngineerTask> tasks,
			Map<String, ScheduleBean> servicerMap, Map<String, Dictionary> taskNatureDic) {
		List<BasicPieChart> taskCateRatio = Lists.newArrayList();// 任务分类占比
		List<HorizontalCapsuleBarGraph> taskStatus = Lists.newArrayList(); // 任务进展
		// 任务性质-任务列表
		Map<String, List<ScreenEngineerTask>> natureTaskListMap = buildNatureTaskListMap(tasks, taskNatureDic);
		//
		for (String nature : natureTaskListMap.keySet()) {
			String natureName = taskNatureDic.get(nature) == null ? null : taskNatureDic.get(nature).getName();
			if (StringUtils.isEmpty(natureName)) {
				natureName = nature;
				if (nature.equals("10")) {
					natureName = "电话支持";
				}
			}
			List<ScreenEngineerTask> lst = natureTaskListMap.get(nature);
			taskCateRatio.add(new BasicPieChart(natureName, (double) lst.size()));
			Map<Integer, List<ScreenEngineerTask>> finishedMap = lst.stream()
					.filter(item -> item.getIfFinished() != null)
					.collect(Collectors.groupingBy(ScreenEngineerTask::getIfFinished));
			for (Integer status : finishedMap.keySet()) {
				List<ScreenEngineerTask> statusList = finishedMap.get(status);
				taskStatus.add(new HorizontalCapsuleBarGraph(natureName, Integer.toString(statusList.size()),
						Integer.toString(status)));
			}
		}
		ScheduleBean bean = servicerMap.get(servicerId);
		if (bean == null) {
			bean = new ScheduleBean();
			servicerMap.put(servicerId, bean);
		}
		bean.setTaskCateRatio(taskCateRatio);
		bean.setTaskStatus(taskStatus);
	}

	private Map<String, List<ScreenEngineerTask>> buildNatureTaskListMap(List<ScreenEngineerTask> tasks,
			Map<String, Dictionary> taskNatureDic) {
		Map<String, List<ScreenEngineerTask>> natureTaskListMap = Maps.newHashMap();
		for (ScreenEngineerTask task : tasks) {
			Integer nature = task.getTaskNature();
			if (nature == null) {
				continue;
			}
			// Integer ifFinished = task.getIfFinished();
			Integer recommend = task.getRecommendedFirstTreatment();
			// Dictionary natureDic = taskNatureDic.get(Integer.toString(nature));
			// String key = natureDic == null ? null : natureDic.getName();
			String key = Integer.toString(nature);
			if (recommend != null && recommend.equals(0)) {
				key = "10";
			}
			List<ScreenEngineerTask> lst = natureTaskListMap.get(key);
			if (lst == null) {
				lst = Lists.newArrayList();
				natureTaskListMap.put(key, lst);
			}
			lst.add(task);
		}
		return natureTaskListMap;
	}

	private void buildServicerName(Map<String, ScheduleBean> servicerMap) {
		List<ServiceOrg> orgList = mapper.scServiceOrgList("0,2", null, false);
		Map<String, ServiceOrg> idServiceOrgMap = orgList.stream()
				.collect(Collectors.toMap(ServiceOrg::getId, Function.identity(), (o, n) -> n));
		for (String sId : idServiceOrgMap.keySet()) {
			ScheduleBean bean = servicerMap.get(sId);
			if (bean == null) {
				bean = new ScheduleBean();
				servicerMap.put(sId, bean);
			}
			bean.setServicer(idServiceOrgMap.get(sId));
		}
	}

	private void buildTaskCountYesterday(DateTime dtStart, DateTime dtEnd, Map<String, ScheduleBean> servicerMap,
			List<ScreenEngineerTask> scEngineerTaskList) {
		List<ScreenEngineerTask> taskListYesterday = scheduleMapper.scEngineerTaskList(//
				dtStart.minusDays(1).toString(DatePattern.YYYYMMDDHHMMSS.val())//
				, dtStart.minusDays(1).toString(DatePattern.YYYYMMDDHHMMSS.val()));
		logger.debug("sc engineer task list size:{}", scEngineerTaskList.size());
		// 当天任务状态 通过 运营数据接口分离出来此块数据内容
		Map<String, List<ScreenEngineerTask>> sIdTaskListMap = taskListYesterday.stream()
				.collect(Collectors.groupingBy(ScreenEngineerTask::getServicerId));
		for (String sId : servicerMap.keySet()) {
			ScheduleBean task = servicerMap.get(sId);
			List<ScreenEngineerTask> lst = sIdTaskListMap.get(sId);
			if (CollectionUtils.isEmpty(lst)) {
				double ratio = task.getTaskCount() == null ? 0 : task.getTaskCount().getValue() * 100d;
				task.setTaskCountYesterday(new MultiText("昨日任务数为:0,环比:" + ratio + "%"));
			} else {
				int taskCountYesterday = lst.size();
				int taskCountToday = (int) task.getTaskCount().getValue();
				double ratio = ((taskCountToday - taskCountYesterday) / (double) taskCountYesterday) * 100d;
				BigDecimal ratioDecimal = new BigDecimal(ratio);
				ratioDecimal.setScale(2, RoundingMode.HALF_UP);
				task.setTaskCountYesterday(
						new MultiText("昨日任务数:" + taskCountYesterday + "环比:" + ratioDecimal.toString() + "%"));
			}
		}
	}

	private void buildTaskCount(Map<String, ScheduleBean> servicerMap,
			Map<String, List<ScreenEngineerTask>> servicerIdListMap) {
		for (String sId : servicerIdListMap.keySet()) {
			ScheduleBean task = servicerMap.get(sId);
			List<ScreenEngineerTask> seTaskList = servicerIdListMap.get(sId);
			int taskCount = 0;
			if (!CollectionUtils.isEmpty(seTaskList)) {
				taskCount = seTaskList.size();
			}
			task.setTaskCount(new DigitalFlop("当前服务任务数", taskCount));
		}
	}

	private void buildFlyLines(Map<String, ScheduleBean> servicerMap,
			Map<String, List<ScreenEngineerTask>> servicerIdListMap) {
		for (String sId : servicerIdListMap.keySet()) {
			ScheduleBean task = servicerMap.get(sId);
			if (task == null) {
				task = new ScheduleBean();
				servicerMap.put(sId, task);
			}
			List<ScreenEngineerTask> seTaskList = servicerIdListMap.get(sId);
			List<FlyLine> flyLines = Lists.newArrayList();
			for (ScreenEngineerTask se : seTaskList) {
				String img = "wait.png";
				// 0-未完工、1-已完工、2-取消、3-已派工
				if (se.getIfFinished() == null) {
				} else if (se.getIfFinished() == 0) {
					img = "unfinished.png";
				} else if (se.getIfFinished() == 1) {
					img = "finished.png";
				} else if (se.getIfFinished() == 2) {
					img = "wait.png";
				} else if (se.getIfFinished() == 3) {
					img = "going.png";
					if (se.getArrivalTime() != null) {// 签到
						img = "sign.png";
					}
				}
				String eName = StringUtils.isEmpty(se.getRealName()) ? se.getUserName() : se.getRealName();

				FlyLine flyLine = null;
				if (se.getIfFinished() == 1 || se.getIfFinished() == 0) { // 完成或者未完成
					flyLine = new FlyLine(//
							se.getCustLng() + "," + se.getCustLat()// 起点 经纬度
							, " "//
							, se.getCustLng() + "," + se.getCustLat()// 终点经纬度
							,
							"<img src='http://smartimages.oss-cn-shanghai.aliyuncs.com/screen/" + img
									+ "' width='32' height='32' title='终点'/><br />"//
									+ se.getCustName());
				} else if (se.getIfFinished() == 3 && se.getArrivalTime() != null) { // 签到
					flyLine = new FlyLine(//
							se.getCustLng() + "," + se.getCustLat()// 起点 经纬度
							,
							"<img src='http://smartimages.oss-cn-shanghai.aliyuncs.com/screen/engineer.png' width='32' height='32' title='起点'/><br />"
									+ eName//
							, se.getCustLng() + "," + se.getCustLat()// 终点经纬度
							,
							"<img src='http://smartimages.oss-cn-shanghai.aliyuncs.com/screen/" + img
									+ "' width='32' height='32' title='终点'/><br />"//
									+ se.getCustName());
				} else {// 派工前往
					flyLine = new FlyLine(//
							se.getEnLng() + "," + se.getEnLat()// 起点 经纬度
							,
							"<img src='http://smartimages.oss-cn-shanghai.aliyuncs.com/screen/engineer.png' width='32' height='32' title='起点'/><br />"
									+ eName//
							, se.getCustLng() + "," + se.getCustLat()// 终点经纬度
							,
							"<img src='http://smartimages.oss-cn-shanghai.aliyuncs.com/screen/" + img
									+ "' width='32' height='32' title='终点'/><br />"//
									+ se.getCustName());
				}
				if (flyLine != null) {
					flyLines.add(flyLine);
				}
			}
			task.setFlyLines(flyLines);
			task.setTaskCount(new DigitalFlop("当前服务任务数", seTaskList.size()));
		}
	}

	public static void main(String[] args) throws UnknownHostException {
		ScreenStarter.main(args);
		new ScreenScheduleHandler().init();
	}
}
