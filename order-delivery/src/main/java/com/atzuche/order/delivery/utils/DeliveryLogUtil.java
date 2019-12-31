package com.atzuche.order.delivery.utils;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.delivery.entity.DeliveryHttpLogEntity;
import com.atzuche.order.delivery.mapper.DeliveryHttpLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author 胡春林
 * 请求仁云数据log
 */
@Component
public class DeliveryLogUtil {

    public static Logger logger = LoggerFactory.getLogger(DeliveryLogUtil.class);

    @Resource
    DeliveryHttpLogMapper deliveryHttpLogMapper;

    public void addDeliveryLog(DeliveryHttpLogEntity deliveryHttpLogEntity) {
        deliveryHttpLogMapper.insert(deliveryHttpLogEntity);
        logger.info("记录发送到仁云日志" + JSON.toJSONString(deliveryHttpLogEntity));
    }

}
