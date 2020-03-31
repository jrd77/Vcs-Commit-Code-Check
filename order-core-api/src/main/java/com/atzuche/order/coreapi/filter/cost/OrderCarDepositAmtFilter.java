package com.atzuche.order.coreapi.filter.cost;

import com.atzuche.order.coreapi.entity.dto.cost.OrderCostContext;
import com.atzuche.order.coreapi.submitOrder.exception.OrderCostFilterException;
import org.springframework.stereotype.Service;

/**
 * 计算订单车辆押金
 *
 * @author pengcheng.fu
 * @date 2020/3/31 11:00
 */
@Service
public class OrderCarDepositAmtFilter implements OrderCostFilter{

    @Override
    public void calculate(OrderCostContext context) throws OrderCostFilterException {

    }
}
