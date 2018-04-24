package com.smartoa.service.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.smartoa.service.model.Agreement;
import com.smartoa.service.model.Cust;
import com.smartoa.service.model.Device;
import com.smartoa.service.model.DeviceLease;
import com.smartoa.service.model.Dictionary;
import com.smartoa.service.model.Engineer;
import com.smartoa.service.model.MapRoadConditions;
import com.smartoa.service.model.Order;
import com.smartoa.service.model.OrderSku;
import com.smartoa.service.model.ServiceOrg;
import com.smartoa.service.model.SysAreaCounty;
import com.smartoa.service.model.SysCity;
import com.smartoa.service.model.SysProvinces;
import com.smartoa.service.model.TsScreenAssets;
import com.smartoa.service.model.WorkOrder;
import com.smartoa.service.model.screen.other.ScreenMember;
import com.smartoa.service.model.screen.other.ScreenProductFieldValue;
import com.smartoa.service.model.screen.other.ScreenRegUser;
import com.smartoa.service.model.screen.other.ScreenServiceArea;
import com.smartoa.service.model.screen.other.ScreenWorkOrderOpr;

public interface ScreenMapper {
	// 订单
	List<Order> scOrderList(@Param("tradeDateStart") String tradeDateStart//
			, @Param("tradeDateEnd") String tradeDateEnd//
			, @Param("orderIds") String orderIds//
	);

	// 区域信息
	List<SysProvinces> scProvinceList();

	List<SysCity> scCityList();

	List<SysAreaCounty> scAreaList();

	// 字典信息
	List<Dictionary> scDicList();

	// 合同,订单单品,客户
	List<Agreement> scAgreementList(@Param("orderIds") String orderIds, @Param("status") Integer status,
			@Param("map") Map<String, Object> map);

	List<OrderSku> scOrderSkuList(@Param("orderNos") String orderNos);

	List<Cust> scCustList(@Param("createTimeStart") String createTimeStart,
			@Param("createTimeEnd") String createTimeEnd, @Param("custIds") String custIds);

	// 工程师, 工单
	List<Engineer> scEngineerList(@Param("createTimeStart") String createTimeStart,
			@Param("createTimeEnd") String createTimeEnd, @Param("ifAuthentication") Integer ifAuthentication);

	List<WorkOrder> scWorkOrderList(@Param("createTimeStart") String createTimeStart,
			@Param("createTimeEnd") String createTimeEnd);

	List<ScreenWorkOrderOpr> scWorkOrderOprList(@Param("start") String start, @Param("end") String end);

	/**
	 * @param orgTypes
	 *            e.g. 0,1...
	 * @param ids
	 *            e.g. 'xxx','x12x'...
	 */
	List<ServiceOrg> scServiceOrgList(@Param("orgTypes") String orgTypes, @Param("ids") String ids,
			@Param("ifAuth") Boolean ifAuth);

	List<DeviceLease> scDeviceLeaseList(@Param("inWork") boolean inWork//
			, @Param("timeStart") Date timeStart//
			, @Param("timeEnd") Date timeEnd);

	List<Device> scDeviceList();

	List<TsScreenAssets> scScreenAssetsList(@Param("withCreateTimeOrder") boolean withCreateTimeOrder,
			@Param("type") String type, @Param("typeId") String typeId, @Param("createTime") Date createTime);

	Integer saveScreenAssets(//
			@Param("type") String type//
			, @Param("customers") int customers //
			, @Param("serviceDevices") int serviceDevices //
			, @Param("certificationServicers") int certificationServicers //
			, @Param("onlineDevices") int onlineDevices //
			, @Param("typeId") String typeId//
			, @Param("createTime") Date createTime//
	);

	Integer updateScreenAssets(//
			@Param("type") String type//
			, @Param("customers") int customers //
			, @Param("serviceDevices") int serviceDevices //
			, @Param("certificationServicers") int certificationServicers //
			, @Param("onlineDevices") int onlineDevices //
			, @Param("typeId") String typeId//
			, @Param("id") Integer id//
	);

	Integer deleteScreenAssets(@Param("createTime") Date createTime);

	List<DeviceLease> scDeviceLeaseList(//
			@Param("timeStart") Date timeStart, //
			@Param("timeEnd") Date timeEnd);

	List<MapRoadConditions> scMapRoadConditions();

	int scSaveVectorData(@Param("adcode") String adcode, @Param("jsonBody") String jsonBody,
			@Param("sourceUrl") String sourceUrl, @Param("createTime") Date createTime,
			@Param("updateTime") Date updateTime);

	int scSaveCentral(@Param("adcode") String adcode, @Param("lng") Double lng, @Param("lat") Double lat,
			@Param("zoom") Integer zoom, @Param("sourceJson") String sourceJson, @Param("sourceUrl") String sourceUrl,
			@Param("createTime") Date createTime, @Param("updateTime") Date updateTime);

	Map<String, Object> scQueryVectorData(String adcode);

	Map<String, Object> scQueryCentral(String adcode);

	/**
	 * 绑定设备查询
	 */
	List<Map<String, Object>> scEngineerDevice(@Param("map") Map<String, Object> map);

	/**
	 * 根据字典类型查询所有产品的属性值信息
	 */
	List<ScreenProductFieldValue> scProductFieldValue(@Param("dictType") String dictType);

	/**
	 * 获取所有注册的会员数
	 */
	List<ScreenRegUser> scRegUsers();

	/**
	 * 获取 由商城过来的客户下的会员
	 */
	List<ScreenMember> scMembers(@Param("start") Date start);

	/**
	 * 获取全部的 服务商 对应的授权区域
	 */
	List<ScreenServiceArea> scServiceAreaList();
}
