package com.atzuche.order.commons;

/**
 *   订单的主状态
 *      待确认 1
 *      待支付 4
 *      待调度 （需要调度的订单才有该状态）8
 *      待取车 16
 *      待还车 32
 *      待结算 64
 *      待违章结算 128
 *      待理赔处理 256
 *      完成  (completed) 512
 *      结束 （closed) 0
 *
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2019/12/9 2:19 下午
 **/
public enum OrderStatus {
      TO_CONFIRM(1,"待确认"),
      TO_PAY(4,"待支付"),
      TO_DISPATCH(8,"待调度"),
      TO_GET_CAR(16,"待取车"),
      TO_RETURN_CAR(32,"待还车"),
      TO_SETTLE(64,"待结算"),
      TO_WZ_SETTLE(128,"待违章结算"),
      TO_CLAIM_SETTLE(256,"待理赔处理"),
      COMPLETED(512,"完成"),
      CLOSED(0,"结束");
      private int status;
      private String desc;

    /**
     *  constructor
     * @param status status value
     * @param desc  status description
     */
    OrderStatus(int status,String desc){
        this.status = status;
        this.desc = desc;
    }

    /**
     * convert int value to OrderStatus
     * @param status int value
     * @return
     */
    public OrderStatus from(int status){
        OrderStatus[] statuses = values();
        for(OrderStatus s:statuses){
            if(status==s.status){
                return s;
            }
        }
        throw new RuntimeException("the value of status :"+status+" not supported,please check");
    }


}
