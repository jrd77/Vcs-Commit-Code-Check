package com.atzuche.order.coreapi.filter.cost.impl;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.entity.dto.CostBaseDTO;
import com.atzuche.order.coreapi.entity.dto.cost.OrderCostContext;
import com.atzuche.order.coreapi.entity.dto.cost.OrderCostDetailContext;
import com.atzuche.order.coreapi.entity.dto.cost.req.OrderCostBaseReqDTO;
import com.atzuche.order.coreapi.entity.dto.cost.res.OrderServiceChargeResDTO;
import com.atzuche.order.coreapi.filter.cost.OrderCostFilter;
import com.atzuche.order.coreapi.submit.exception.OrderCostFilterException;
import com.atzuche.order.rentercost.entity.RenterOrderCostDetailEntity;
import com.atzuche.order.rentercost.service.RenterOrderCostCombineService;
import com.autoyol.commons.web.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 计算手续费
 *
 * @author pengcheng.fu
 * @date 2020/3/30 11:09
 */
@Service
@Slf4j
public class OrderServiceChargeFilter implements OrderCostFilter {

    @Autowired
    private RenterOrderCostCombineService renterOrderCostCombineService;

    @Override
    public void calculate(OrderCostContext context) throws OrderCostFilterException {
        OrderCostBaseReqDTO baseReqDTO = context.getReqContext().getBaseReqDTO();
        log.info("订单费用计算-->手续费.param is,baseReqDTO:[{}]", JSON.toJSONString(baseReqDTO));

        if (Objects.isNull(baseReqDTO)) {
            throw new OrderCostFilterException(ErrorCode.PARAMETER_ERROR.getCode(), "计算手续费参数为空!");
        }
        //基础信息
        CostBaseDTO costBaseDTO = new CostBaseDTO();
        BeanUtils.copyProperties(baseReqDTO, costBaseDTO);
        RenterOrderCostDetailEntity serviceChargeFeeEntity = renterOrderCostCombineService.getServiceChargeFeeEntity(costBaseDTO);
        log.info("订单费用计算-->手续费.serviceChargeFeeEntity:[{}]", JSON.toJSONString(serviceChargeFeeEntity));

        OrderServiceChargeResDTO orderServiceChargeResDTO = new OrderServiceChargeResDTO();
        if (null != serviceChargeFeeEntity) {
            orderServiceChargeResDTO.setServiceCharge(serviceChargeFeeEntity.getTotalAmount());
            orderServiceChargeResDTO.setDetail(serviceChargeFeeEntity);

            //赋值OrderCostDetailContext
            OrderCostDetailContext costDetailContext = context.getCostDetailContext();
            costDetailContext.getCostDetails().add(serviceChargeFeeEntity);
        }
        log.info("订单费用计算-->手续费.result is,orderServiceChargeResDTO:[{}]", JSON.toJSONString(orderServiceChargeResDTO));
        context.getResContext().setOrderServiceChargeResDTO(orderServiceChargeResDTO);
    }
}
