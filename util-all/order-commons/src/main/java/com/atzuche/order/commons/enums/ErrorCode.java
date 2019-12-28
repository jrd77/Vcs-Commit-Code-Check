package com.atzuche.order.commons.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ACCOUT_DEBT_DEDUCT_DEBT("921001","抵扣历史欠款出错"),
    ACCOUT_DEBT_INSERT_DEBT("921002","记录历史欠款出错"),

    ACCOUT_OWNER_COST_SETTLE("931001","车主结算出错"),

    ACCOUT_OWNER_INCOME_SETTLE("941001","车主收益结算出错"),
    ACCOUT_OWNER_INCOME_EXAMINE("941002","车主收益审核出错"),

    ACCOUT_RENTER_CLAIM_DETAIL("881001","租客理赔费用操作失败"),

    ACCOUT_RENTET_DEPOSIT_FAIL("961001","车俩应收押金操作失败"),
    ACCOUT_RENTET_COST_FAIL("961004","车俩租车费用收银台操作失败"),
    CHANGE_ACCOUT_RENTET_DEPOSIT_FAIL("961003","车俩押金资金进出操作失败"),

    ACCOUT_RENTER_DETAIL_DETAIL("891001","暂扣押金失败"),

    ACCOUT_RENTER_COST_DETAIL("951001","租车费用明细操作失败"),
    ACCOUT_RENTER_COST_SETTLE("951002","租车费用操作失败"),
    ACCOUT_RENTER_COST_SETTLE_REFUND("951003","租车费用退还操作失败"),

    ACCOUT_RENTER_STOP_DETAIL("871001","租客停运费用操作失败"),

    ACCOUT_RENTET_WZ_DEPOSIT_FAIL("971001","违章押金操作失败"),
    ACCOUT_RENTET_WZ_COST_FAIL("971002","违章费用操作失败"),
    CHANGE_ACCOUT_RENTET_WZ_DEPOSIT_FAIL("971003","违章押金资金进出操作失败"),

    PLATFORM_SETTLE_SUBSIDY_AND_PROFIT("871001","结算平台费用出错"),

    CASHIER_REFUND_APPLY("981001","退款申请出错"),
    CASHIER_PAY_REFUND_CALL_BACK_FAIL("981003","支付系统退款回调操作失败"),
    CASHIER_PAY_CALL_BACK_FAIL("981004","支付系统支付回调操作失败"),

    GET_WALLETR_MSG("981005","查询钱包信息出错"),
    DEDUCT_WALLETR_MSG("981006","扣减钱包信息出错"),


    ORDER_RENTER_ORDERNO_CREATE_ERROR("600001","订单编码创建异常"),


    COST_GET_RETUIRN_ERROR("700001","获取取还车费用系统异常"),
    COST_GET_RETUIRN_FAIL("700002","获取取还车费用失败"),
    COST_GET_RETUIRN_OVER_ERROR("700003","获取取还车费用异常"),
    COST_GET_RETUIRN_OVER_FAIL("700004","获取取还车费用失败"),
    IS_GET_CAR_OVER_FAIL("700005","取车是否超运能获取失败"),
    IS_GET_CAR_OVER_ERROR("700006","取车是否超运能接口异常"),
    IS_RETURN_CAR_OVER_FAIL("700007","还车是否超运能获取失败"),
    IS_RETURN_CAR_OVER_ERROR("700008","还车是否超运能接口异常")
    ;

    private String code;
    private String text;

    private ErrorCode(String code, String text) {
        this.code = code;
        this.text = text;
    }
}
