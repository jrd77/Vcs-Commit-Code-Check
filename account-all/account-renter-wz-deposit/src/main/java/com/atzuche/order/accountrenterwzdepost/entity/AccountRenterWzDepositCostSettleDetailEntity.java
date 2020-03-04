package com.atzuche.order.accountrenterwzdepost.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;


/**
 * 违章费用结算明细表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:58:17
 * @Description:
 */
@Data
public class AccountRenterWzDepositCostSettleDetailEntity implements Serializable {
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
     * 会员号
     */
    private String memNo;
    /**
     * 单位
     */
    private Integer unit;
    /**
     * 单价 负
     */
    private Integer price;
    /**
     * 违章费用
     */
    private Integer wzAmt;
    /**
     * 费用来源凭证
     */
    private String uniqueNo;

    /**
     * 费用编码
     */
    private String costCode;

    /**
     * 费用描述
     */
    private String costDetail;

    /**
     * 费用类型(1 罚金) 10.违章
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建人
     */
    private String createOp;
    /**
     * 创建时间
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
