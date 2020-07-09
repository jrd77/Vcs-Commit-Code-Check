package com.atzuche.order.ownercost.entity.dto;

import com.atzuche.order.ownercost.entity.OwnerOrderPurchaseDetailEntity;
import com.atzuche.order.ownercost.entity.OwnerOrderSubsidyDetailEntity;
import com.autoyol.doc.annotation.AutoDocProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OwnerOrderReqDTO {
    /**
     * 主订单号
     */
    private String orderNo;
    /**
     * 车主子订单号
     */
    private String ownerOrderNo;
    /**
     * 租客子订单号
     */
    private String renterOrderNo;
    /**
     * 会员号
     */
    private String memNo;
    /**
     * 显示起租时间
     */
    private LocalDateTime showRentTime;
    /**
     * 显示还车时间
     */
    private LocalDateTime showRevertTime;
    /**
     * 预计起租时间
     */
    private LocalDateTime expRentTime;
    /**
     * 预计还车时间
     */
    private LocalDateTime expRevertTime;
    /**
     * 应答标识位，0未设置，1已设置
     */
    private Integer replyFlag;
    /*
    *
    * 自动应答标志位 最终值
    * */
    private boolean isAutoReplyFlag;
    /**
     * 是否使用特供价 0-否，1-是
     */
    private Integer isUseSpecialPrice;
    /**
     * 车辆类型
     */
    private Integer carOwnerType;
    /**
     * 取车标志
     */
    private Integer srvGetFlag;
    /**
     * 还车标志
     */
    private Integer srvReturnFlag;
    /**
     * 车辆号
     */
    private String carNo;
    /**
     * 1、短租 2、套餐
     */
    private Integer category;

    /**
     * 平台服务费比例（仅车主端有）
     */
    private Double serviceRate;

    /**
     * 代管车服务费比例（仅车主端有）
     */
    private Double serviceProxyRate;

    /**
     * 固定平台服务费比例
     */
    @AutoDocProperty("固定平台服务费比例")
    private Double fixedServiceRate;
    /**
     * 当前使用的服务费比例(平台服务费比例/代官车服务费比例/固定平台服务费比例)
     */
    @AutoDocProperty("当前使用的服务费比例(平台服务费比例/代官车服务费比例/固定平台服务费比例)")
    private Double useServiceRate;

    /**
     * GPS序号
     */
    private String gpsSerialNumber;

    /**
     * 补贴费用明细（车主券）
     */
    private List<OwnerOrderSubsidyDetailEntity> ownerOrderSubsidyDetails;

    /**
     * 租金明细
     */
    private OwnerOrderPurchaseDetailEntity ownerOrderPurchaseDetailEntity;
}
