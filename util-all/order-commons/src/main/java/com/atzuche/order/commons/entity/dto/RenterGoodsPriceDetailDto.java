package com.atzuche.order.commons.entity.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
public class RenterGoodsPriceDetailDto {
    /**
     * 主订单号
     */
    private String orderNo;
    /**
     * 子订单号
     */
    private String renterOrderNo;
    /**
     * 商品概览id
     */
    private Integer goodsId;
    /**
     * 天
     */
    private LocalDate carDay;
    /**
     * 天单价
     */
    private Integer carUnitPrice;
    /**
     * 小时数
     */
    private Float carHourCount;
    /**
     * 还车时间
     */
    private LocalDateTime revertTime;
}
