package com.atzuche.order.commons.vo.res.cost;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


/**
 * 租客端配送订单表
 *
 * @author 胡春林
 * @date 2019-12-28 15:55:26
 * @Description:
 */
@Data
public class RenterOrderDeliveryResVO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	private Integer id;
	/**
	 * 主订单号
	 */
	private String orderNo;
	/**
	 * 子订单号
	 */
	private String renterOrderNo;
	/**
	 * 配送订单号
	 */
	private String orderNoDelivery;
	/**
	 * 配送类型 1-取车订单、2-还车订单
	 */
	private Integer type;
	/**
	 * 起租时间
	 */
//	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime rentTime;
	/**
	 * 归还时间
	 */
//	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private LocalDateTime revertTime;
	/**
	 * 城市编码
	 */
	private String cityCode;
	/**
	 * 车主姓名
	 */
	private String ownerName;
	/**
	 * 车主电话
	 */
	private String ownerPhone;
	/**
	 * 租客姓名
	 */
	private String renterName;
	/**
	 * 租客已成交次数
	 */
	private Integer renterDealCount;
	/**
	 * 租客电话
	 */
	private String renterPhone;
	/**
	 * 城市名称
	 */
	private String cityName;
	/**
	 * 取还车人员
	 */
	private String getReturnUserName;
	/**
	 * 取还车人员手机号码
	 */
	private String getReturnUserPhone;
	/**
	 * 租客取还车地址
	 */
	private String renterGetReturnAddr;
	/**
	 * 租客取还车经度
	 */
	private String renterGetReturnAddrLon;
	/**
	 * 租客取还车维度
	 */
	private String renterGetReturnAddrLat;
	/**
	 * 车主取还车地址
	 */
	private String ownerGetReturnAddr;
	/**
	 * 车主取还车经度
	 */
	private String ownerGetReturnAddrLon;
	/**
	 * 车主取还车维度
	 */
	private String ownerGetReturnAddrLat;
	/**
	 * 状态
	 */
	private Integer status;
	/**
	 * 是否通知到仁云 0-否，1-是
	 */
	private Integer isNotifyRenyun;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 创建人
	 */
	private String createOp;
	/**
	 * 修改时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 修改人
	 */
	private String updateOp;
	/**
	 * 0-正常，1-已逻辑删除
	 */
	private Integer isDelete;

	private Integer aheadOrDelayTime;


    public void setAheadOrDelayTimeInfo(Integer getMinutes, Integer returnMinutes) {
        if (Objects.isNull(getMinutes) && Objects.isNull(returnMinutes)) {
            setAheadOrDelayTime(0);
        } else {
            int aheadOrDelayTime = getMinutes == null ? returnMinutes : getMinutes;
            setAheadOrDelayTime(aheadOrDelayTime);
        }

    }
}
