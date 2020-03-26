package com.atzuche.order.accountownerincome.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * 车主收益待审核表
 * 
 * @author ZhangBin
 * @date 2020-02-21 20:20:35
 * @Description:
 */
@Data
public class AccountOwnerIncomeExamineEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	private Integer id;
	/**
	 * 会员号
	 */
	private String memNo;
	/**
	 * 主订单号
	 */
	private String orderNo;
	/**
	 * 车主子订单
	 */
	private String ownerOrderNo;
	/**
	 * 收益审核金额
	 */
	private Integer amt;
	/**
	 * 收益审核描述
	 */
	private String detail;
	/**
	 * 审核状态1,待审核 2,审核通过 3,审核拒绝
	 */
	private Integer status;
	/**
	 * 类型，1收益，2调账
	 */
	private Integer type;
	/**
	 * 审核人
	 */
	private String opName;
	/**
	 * 审核时间
	 */
	private LocalDateTime time;
    /*
    *
    * */
	private Integer examineId;

	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 更新版本号
	 */
	private Integer version;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 更新人
	 */
	private String createOp;
	/**
	 * 修改时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 更新人
	 */
	private String updateOp;
	/**
	 * 0-正常，1-已逻辑删除
	 */
	private Integer isDelete;

}
