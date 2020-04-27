package com.atzuche.order.sms.service;

import com.alibaba.fastjson.JSONObject;
import com.atzuche.order.sms.common.base.OrderSendMessageFactory;
import com.atzuche.order.sms.common.sms.SMSOrderBaseEventService;
import com.atzuche.order.sms.enums.PushMessageTypeEnum;
import com.atzuche.order.sms.enums.ShortMessageTypeEnum;
import com.atzuche.order.sms.utils.SMSIcsocVoiceUtils;
import com.atzuche.order.sms.utils.SmsParamsMapUtil;
import com.autoyol.commons.web.ErrorCode;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 胡春林
 */
@Service
public class SendShortMessageDataService {

    private Logger logger = LoggerFactory.getLogger(SendShortMessageDataService.class);

    @Autowired
    SMSOrderBaseEventService smsOrderBaseEventService;
    @Autowired
    OrderSendMessageFactory orderSendMessageFactory;

    /**
     * 違章未支付發送
     * @param condition
     * @param orderNo
     */
    public void sendShortMessageData(boolean condition, String orderNo) {
        if (condition) {
            Map map = SmsParamsMapUtil.getParamsMap(orderNo, null, ShortMessageTypeEnum.REMIND_PAY_ILLEGAL_DEPOSITOWNER.getValue(), null);
            smsOrderBaseEventService.sendShortMessage(map);
        }
    }

    /**
     * 未支付租车押金或违章押金
     * @param orderNo
     */
    public void sendNoPayShortMessageData(String orderNo, String typeName) {
        Map paramsMap = Maps.newHashMap();
        paramsMap.put("PayType", typeName);
        Map map = SmsParamsMapUtil.getParamsMap(orderNo, null, null, paramsMap);
        smsOrderBaseEventService.sendShortMessage(map);
        Map pushMap = SmsParamsMapUtil.getParamsMap(orderNo, PushMessageTypeEnum.RENTER_NO_PAY_ILLEGAL_CANCEL.getValue(), PushMessageTypeEnum.RENTER_NO_PAY_ILLEGAL_2_OWNER.getValue(), null);
        orderSendMessageFactory.sendPushMessage(pushMap);
    }

    /**
     * 未支付违章押金
     * @param orderNo
     */
    public void sendNoPayIllegalDepositShortMessageData(String orderNo) {
        Map map = SmsParamsMapUtil.getParamsMap(orderNo, ShortMessageTypeEnum.DISTRIBUTE_PAYILLEGAL_DEPOSIT_CANCEL_RENTER.getValue(), ShortMessageTypeEnum.PAY_ILLEGAL_DEPOSIT_CANCEL_OWNER.getValue(), null);
        smsOrderBaseEventService.sendShortMessage(map);
        Map pushMap = SmsParamsMapUtil.getParamsMap(orderNo, PushMessageTypeEnum.RENTER_NO_PAY_ILLEGAL_CANCEL.getValue(), PushMessageTypeEnum.RENTER_NO_PAY_ILLEGAL_2_OWNER.getValue(), null);
        orderSendMessageFactory.sendPushMessage(pushMap);
    }


    /**
     * 距离支付结束时间只有30分钟时，如还未支付租车费用
     * @param orderNo
     */
    public void sendNoPayCarShortMessageData(String orderNo) {
        Map map = SmsParamsMapUtil.getParamsMap(orderNo, ShortMessageTypeEnum.CANCLE_ORDER_WARNINGF_OR_FREEDEPOSIT.getValue(), null, null);
        smsOrderBaseEventService.sendShortMessage(map);
    }

    /**
     * 车主同意后-未支付租车押金-每15分钟提醒一次
     * @param orderNo
     */
    public void sendNoPayCarCostShortMessageData(String orderNo, Map paramsMap) {
        Map map = SmsParamsMapUtil.getParamsMap(orderNo, ShortMessageTypeEnum.NO_EXEMPT_PREORDER_REMIND_PAYRENT.getValue(), null, paramsMap);
        smsOrderBaseEventService.sendShortMessage(map);
    }

    /**
     * 發送語音短信
     * @param orderNo
     * @param paramsMap
     */
    public void sendVoiceRemindVoicePayIllegalCrashData(boolean condition, String orderNo, Map paramsMap) {
        if (!condition) {
            return;
        }
        Map map = SmsParamsMapUtil.getParamsMap(orderNo, ShortMessageTypeEnum.REMIND_PAY_ILLEGAL_DEPOSITRENTER.getValue(), null, paramsMap);
        smsOrderBaseEventService.sendShortMessage(map);
    }

    /**
     * 發送用戶未支付違章押金語音提醒
     * @param renterMobilePhone
     */
    public void sendVoiceRemindVoicePayIllegalCrashWithHoursData(boolean condition, String mainOrderNo, String renterMobilePhone, String expReterTime) {
        if (!condition) {
            return;
        }
        Map<String, Object> paramMap = new HashMap<>();
        String orderNo = mainOrderNo;
        String jobId = mainOrderNo;
        String renterPhone = renterMobilePhone;
        StringBuilder content = new StringBuilder();
        content.append("您还未支付预定车辆的押金，请在").append(expReterTime).append("前完成支付。否则该订单将被取消，并扣除您的违约费用");
        paramMap.put("content", content.toString());
        paramMap.put("renterPhone", renterPhone);
        paramMap.put("orderNo", orderNo);
        paramMap.put("type", 3);
        String createTime = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        paramMap.put("createTime", createTime);
        paramMap.put("proId", 6);
        paramMap.put("platform", 1);
        paramMap.put("jobId", jobId);
        paramMap.put("order_change_wel", "凹凸租车提醒您");
        paramMap.put("delayTips", content.toString());
        ErrorCode errorCode = SMSIcsocVoiceUtils.getIcsocNewServer(paramMap);
        logger.info("发送语音短信,参数--->>>>:", JSONObject.toJSONString(paramMap));
        if (Objects.nonNull(errorCode) && errorCode.getCode().equals(ErrorCode.SUCCESS.getCode())) {
            logger.info("发送语音短信成功--->>>>订单号：{},手机号：{}", orderNo, renterMobilePhone);
        }
    }
}
