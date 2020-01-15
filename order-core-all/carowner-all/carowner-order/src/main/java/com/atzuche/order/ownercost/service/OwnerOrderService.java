package com.atzuche.order.ownercost.service;


import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.entity.dto.CostBaseDTO;
import com.atzuche.order.commons.enums.OwnerChildStatusEnum;
import com.atzuche.order.ownercost.entity.OwnerOrderEntity;
import com.atzuche.order.ownercost.entity.dto.OwnerOrderCostReqDTO;
import com.atzuche.order.ownercost.entity.dto.OwnerOrderReqDTO;
import com.atzuche.order.ownercost.mapper.OwnerOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OwnerOrderService {
    @Autowired
    private OwnerOrderMapper ownerOrderMapper;
    @Autowired
    private OwnerOrderCalCostService ownerOrderCalCostService;

    /*
     * @Author ZhangBin
     * @Date 2020/1/10 14:30
     * @Description: 获取待生效的子订单状态
     * 
     **/
    public OwnerOrderEntity getChangeOwnerByOrderNo(String orderNo){
        return ownerOrderMapper.getChangeOwnerByOrderNo(orderNo);
    }


    /*
     * @Author ZhangBin
     * @Date 2019/12/25 10:08
     * @Description: 查询有效的子订单
     *
     **/
    public OwnerOrderEntity getOwnerOrderByOrderNoAndIsEffective(String orderNo){
        return ownerOrderMapper.getOwnerOrderByOrderNoAndIsEffective(orderNo);
    }
    
    /**
     * 根据id把上笔车主子单置为无效
     * @param id
     * @return Integer
     */
    public Integer updateOwnerOrderInvalidById(Integer id) {
    	return ownerOrderMapper.updateOwnerOrderInvalidById(id);
    }
    
    /**
     * 保存车主子订单
     * @param ownerOrderEntity
     * @return Integer
     */
    public Integer saveOwnerOrder(OwnerOrderEntity ownerOrderEntity) {
    	return ownerOrderMapper.insertSelective(ownerOrderEntity);
    }


    /*
     * @Author ZhangBin
     * @Date 2019/12/31 14:45
     * @Description: 生成车主订单
     * 
     **/
    public void generateRenterOrderInfo(OwnerOrderReqDTO ownerOrderReqDTO){
        //1、生成车主子订单
        OwnerOrderEntity ownerOrderEntity = new OwnerOrderEntity();
        BeanUtils.copyProperties(ownerOrderReqDTO,ownerOrderEntity);
        ownerOrderEntity.setGoodsCode(ownerOrderReqDTO.getCarNo());
        ownerOrderEntity.setGoodsType(String.valueOf(ownerOrderReqDTO.getCategory()));
        ownerOrderEntity.setChildStatus(OwnerChildStatusEnum.PROCESS_ING.getCode());
        log.info("下单-车主端-生成车主子订单ownerOrderEntity=[{}]",JSON.toJSONString(ownerOrderEntity));
        int result = ownerOrderMapper.insertSelective(ownerOrderEntity);
        log.info("下单-车主端-生成车主子订单结果result=[{}],ownerOrderEntity=[{}]",result,JSON.toJSONString(ownerOrderEntity));

        //2、生成费用信息
        OwnerOrderCostReqDTO ownerOrderCostReqDTO = new OwnerOrderCostReqDTO();
        ownerOrderCostReqDTO.setCarOwnerType(ownerOrderReqDTO.getCarOwnerType());
        ownerOrderCostReqDTO.setSrvGetFlag(ownerOrderReqDTO.getSrvGetFlag());
        ownerOrderCostReqDTO.setSrvReturnFlag(ownerOrderReqDTO.getSrvReturnFlag());
        ownerOrderCostReqDTO.setOwnerOrderPurchaseDetailEntity(ownerOrderReqDTO.getOwnerOrderPurchaseDetailEntity());
        ownerOrderCostReqDTO.setOwnerOrderSubsidyDetailEntity(ownerOrderReqDTO.getOwnerOrderSubsidyDetailEntity());

        CostBaseDTO costBaseDTO = new CostBaseDTO();
        costBaseDTO.setOrderNo(ownerOrderReqDTO.getOrderNo());
        costBaseDTO.setOwnerOrderNo(ownerOrderReqDTO.getOwnerOrderNo());
        costBaseDTO.setMemNo(ownerOrderReqDTO.getMemNo());
        ownerOrderCostReqDTO.setCostBaseDTO(costBaseDTO);
        log.info("下单-车主端-准备保存车主费用明细 ownerOrderNo=[{}],ownerOrderCostReqDTO=[{}]", ownerOrderReqDTO.getOwnerOrderNo(),JSON.toJSONString(ownerOrderCostReqDTO));
        ownerOrderCalCostService.getOrderCostAndDeailList(ownerOrderCostReqDTO);

    }


    public Integer updateOwnerOrderChildStatus(Integer id, Integer childStatus) {
        return ownerOrderMapper.updateOwnerOrderChildStatus(id, childStatus);
    }

    public List<OwnerOrderEntity> queryHostiryOwnerOrderByOrderNo(String orderNo) {
        return ownerOrderMapper.queryHostiryOwnerOrderByOrderNo(orderNo);
    }

    public OwnerOrderEntity queryCancelOwnerOrderByOrderNoIsEffective(String orderNo) {
       return ownerOrderMapper.queryCancelOwnerOrderByOrderNoIsEffective(orderNo);
    }
}
