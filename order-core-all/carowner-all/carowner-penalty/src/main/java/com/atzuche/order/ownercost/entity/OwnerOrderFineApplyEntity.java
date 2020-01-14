package com.atzuche.order.ownercost.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 车主取消订单违约罚金
 * 
 * @author ZhangBin
 * @date 2020-01-14 19:39:44
 */
@Data
public class OwnerOrderFineApplyEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 唯一主键
	 */
	private Integer id;
	/**
	 * 主订单号
	 */
	private String orderNo;
	/**
	 * 子订单号
	 */
	private String ownerOrderNo;
	/**
	 * 会员号
	 */
	private Integer memNo;
	/**
	 * 罚金来源编码（车主/租客/平台）1-租客，2-车主，3-平台
	 */
	private String fineSubsidySourceCode;
	/**
	 * 罚金来源描述
	 */
	private String fineSubsidySourceDesc;
	/**
	 * 罚金金额
	 */
	private Integer fineAmount;
	/**
	 * 罚金类型：1-车主修改交接车地址罚金，4-取消订单违约金
	 */
	private Integer fineType;
	/**
	 * 罚金类型描述
	 */
	private String fineTypeDesc;
	/**
	 * 备注
	 */
	private String remark;
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
