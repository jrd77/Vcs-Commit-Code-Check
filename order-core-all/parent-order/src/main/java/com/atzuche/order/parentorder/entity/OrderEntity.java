package com.atzuche.order.parentorder.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;


/**
 * 主订单表
 * 
 * @author ZhangBin
 * @date 2019-12-24 16:19:33
 */
@Data
public class OrderEntity implements Serializable {

    private static final long serialVersionUID = 7867901303046583923L;

    /**
	 * 
	 */
	private Integer id;
	/**
	 * 主订单号
	 */
	private String orderNo;
	/**
	 * 租客会员号
	 */
	private Integer memNoRenter;
	/**
	 * 订单类型（内部分类）1：短租， 2：套餐
	 */
	private Integer category;
	/**
	 * 场景号
	 */
	private String entryCode;
	/**
	 * 来源 1：手机，2：网站，3:管理后台，4:cp b2c, 5:cp upop
	 */
	private Integer source;
	/**
	 * 预计起租时间
	 */
	private LocalDateTime expRentTime;
	/**
	 * 预计还车时间
	 */
	private LocalDateTime expRevertTime;
	/**
	 * 下单城市名称
	 */
	private String cityName;
	/**
	 * 下单城市code
	 */
	private String cityCode;
	/**
	 * 是否出市 0-否，1-是
	 */
	private Integer isOutCity;
	/**
	 * 是否免押 0-否，1-是
	 */
	private Integer isFreeDeposit;
	/**
	 * 是否使用机场服务 0-否，1-是
	 */
	private Integer isUseAirPortService;
	/**
	 * 请求时间
	 */
	private LocalDateTime reqTime;
	/**
	 * 风控审核id
	 */
	private Integer riskAuditId;
	/**
	 * 版本号
	 */
	private Integer version;
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

}
