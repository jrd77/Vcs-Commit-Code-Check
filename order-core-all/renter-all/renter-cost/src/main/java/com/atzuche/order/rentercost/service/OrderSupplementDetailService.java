package com.atzuche.order.rentercost.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atzuche.order.rentercost.entity.OrderSupplementDetailEntity;
import com.atzuche.order.rentercost.mapper.OrderSupplementDetailMapper;



/**
 * 订单补付表
 *
 */
@Service
public class OrderSupplementDetailService{
    @Autowired
    private OrderSupplementDetailMapper orderSupplementDetailMapper;


    /**
     * 获取租客补付记录
     * @param orderNo 主订单号
     * @param memNo 会员号
     * @return List<OrderSupplementDetailEntity>
     */
    public List<OrderSupplementDetailEntity> listOrderSupplementDetailByOrderNoAndMemNo(String orderNo, String memNo) {
    	return orderSupplementDetailMapper.listOrderSupplementDetailByOrderNoAndMemNo(orderNo, memNo);
    }
    
    /**
     * 保存租客补付记录
     * @param supplementEntity 补付记录
     * @return Integer
     */
    public Integer saveOrderSupplementDetail(OrderSupplementDetailEntity supplementEntity) {
    	return orderSupplementDetailMapper.insertSelective(supplementEntity);
    }
    
    /**
     * 更新补付支付状态
     * @param id
     * @param payFlag
     * @return Integer
     */
    public Integer updatePayFlagById(Integer id, Integer payFlag) {
    	return orderSupplementDetailMapper.updatePayFlagById(id, payFlag);
    }
    
    /**
     * 根据订单号获取补付记录
     * @param orderNo
     * @return List<OrderSupplementDetailEntity>
     */
    public List<OrderSupplementDetailEntity> listOrderSupplementDetailByOrderNo(String orderNo) {
    	return orderSupplementDetailMapper.listOrderSupplementDetailByOrderNo(orderNo);
    }
    
    public Integer updateDeleteById(Integer id) {
    	return orderSupplementDetailMapper.updateDeleteById(id);
    }
}
