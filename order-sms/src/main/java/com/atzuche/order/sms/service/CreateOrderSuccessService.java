package com.atzuche.order.sms.service;

import com.atzuche.order.sms.common.annatation.OrderService;
import com.atzuche.order.sms.common.annatation.SMS;
import com.atzuche.order.sms.interfaces.IOrderRouteKeyMessage;

import java.util.Map;

/**
 * @author 胡春林
 * 发送订单创建成功事件(套餐SMS)
 */
@OrderService
public class CreateOrderSuccessService implements IOrderRouteKeyMessage<Map> {

    @Override
    @SMS(renterFlag = "NotifyRenterTransReqAcceptedPackage")
    public void sendOrderMessageWithNo() {

    }

    @Override
    public Map hasElseOtherParams(Map paramsMap) {
        return null;
    }
}
