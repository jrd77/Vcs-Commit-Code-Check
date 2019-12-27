package com.atzuche.order.coreapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atzuche.order.commons.entity.dto.RenterMemberDTO;
import com.atzuche.order.coreapi.entity.request.ModifyOrderReq;
import com.atzuche.order.rentermem.service.RenterMemberService;
import com.atzuche.order.renterorder.entity.RenterOrderEntity;
import com.atzuche.order.renterorder.service.RenterOrderService;
import com.autoyol.commons.web.ResponseData;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ModifyOrderService {
	@Autowired
	private UniqueOrderNoService uniqueOrderNoService;
	@Autowired
	private RenterOrderService renterOrderService;
	@Autowired
	private RenterMemberService renterMemberService;

	/**
	 * 修改订单主逻辑
	 * @param modifyOrderReq
	 * @return ResponseData
	 */
	public ResponseData<?> modifyOrder(ModifyOrderReq modifyOrderReq) {
		log.info("modifyOrder修改订单主逻辑入参modifyOrderReq=[{}]", modifyOrderReq);
		// 主单号
		String orderNo = modifyOrderReq.getOrderNo();
		// 获取修改前有效租客子订单信息
		RenterOrderEntity renterOrderEntity = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
		// 获取租客会员信息
		RenterMemberDTO renterMemberDTO = renterMemberService.selectrenterMemberByMemNo(renterOrderEntity.getRenterOrderNo(), true);
		// 获取租客商品信息
		
		// TODO DTO包装
		
		// TODO 前置校验
		
		// 获取租客订单号
		String renterOrderNo = uniqueOrderNoService.getRenterOrderNo(orderNo);
		
		// TODO 风控校验
		
		// TODO 库存校验
		
		// 基础费用计算
		
		// TODO 取还车费用计算
		
		// TODO 超运能费用计算
		
		// 违约金计算
		
		// TODO 车主券
		
		// TODO 限时立减
		
		// TODO 取还车券
		
		// TODO 平台券
		
		// TODO 凹凸币
		
		// 修改前费用
		
		// 修改后费用
		
		// 入库
		
		
		
		return null;
	}
	
}
