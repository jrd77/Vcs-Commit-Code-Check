package com.atzuche.order.delivery.service.delivery;

import com.atzuche.order.commons.OrderReqContext;
import com.atzuche.order.commons.entity.dto.OwnerMemberDTO;
import com.atzuche.order.commons.entity.dto.RenterGoodsDetailDTO;
import com.atzuche.order.commons.entity.dto.RenterMemberDTO;
import com.atzuche.order.commons.vo.req.NormalOrderReqVO;
import com.atzuche.order.delivery.common.DeliveryCarTask;
import com.atzuche.order.delivery.common.DeliveryErrorCode;
import com.atzuche.order.delivery.entity.RenterDeliveryAddrEntity;
import com.atzuche.order.delivery.entity.RenterOrderDeliveryEntity;
import com.atzuche.order.delivery.enums.DeliveryTypeEnum;
import com.atzuche.order.delivery.enums.ServiceTypeEnum;
import com.atzuche.order.delivery.enums.UsedDeliveryTypeEnum;
import com.atzuche.order.delivery.exception.DeliveryOrderException;
import com.atzuche.order.delivery.mapper.RenterDeliveryAddrMapper;
import com.atzuche.order.delivery.mapper.RenterOrderDeliveryMapper;
import com.atzuche.order.delivery.utils.DateUtils;
import com.atzuche.order.delivery.vo.delivery.*;
import com.autoyol.commons.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author 胡春林
 * 配送服务
 */
@Service
public class DeliveryCarService {

    @Autowired
    DeliveryCarTask deliveryCarTask;
    @Resource
    RenterDeliveryAddrMapper deliveryAddrMapper;
    @Resource
    RenterOrderDeliveryMapper orderDeliveryMapper;

    /**
     * 添加配送相关信息(是否下单，是否推送仁云)
     */
    public void addRenYunFlowOrderInfo(OrderReqContext orderReqContext) {
        OrderDeliveryVO orderDeliveryVO = createOrderDeliveryParams(orderReqContext);
        if (null == orderDeliveryVO || orderDeliveryVO.getRenterDeliveryAddrDTO() == null) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        insertDeliveryAddress(orderDeliveryVO);
        if (orderDeliveryVO.getOrderDeliveryDTO() != null && orderDeliveryVO.getOrderDeliveryDTO().getIsNotifyRenyun().intValue() == UsedDeliveryTypeEnum.USED.getValue().intValue()) {
            RenYunFlowOrderDTO renYunFlowOrder = orderDeliveryVO.getRenYunFlowOrderDTO();
            deliveryCarTask.addRenYunFlowOrderInfo(renYunFlowOrder);
        }
    }

    /**
     * 更新配送订单到仁云流程系统
     */
    public void updateRenYunFlowOrderInfo(UpdateOrderDeliveryVO updateFlowOrderVO) {
        if (null == updateFlowOrderVO || updateFlowOrderVO.getRenterDeliveryAddrDTO() == null) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        OrderDeliveryVO orderDeliveryVO = new OrderDeliveryVO();
        orderDeliveryVO.setOrderDeliveryDTO(updateFlowOrderVO.getOrderDeliveryDTO());
        orderDeliveryVO.setRenterDeliveryAddrDTO(updateFlowOrderVO.getRenterDeliveryAddrDTO());
        insertDeliveryAddress(orderDeliveryVO);
        deliveryCarTask.updateRenYunFlowOrderInfo(updateFlowOrderVO.getUpdateFlowOrderDTO());
    }

    /**
     * 取消配送订单到仁云流程系统
     */
    public void cancelRenYunFlowOrderInfo(CancelOrderDeliveryVO cancelOrderDeliveryVO) {
        if (null == cancelOrderDeliveryVO || cancelOrderDeliveryVO.getCancelFlowOrderDTO() == null || StringUtils.isBlank(cancelOrderDeliveryVO.getRenterOrderNo())) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        int serviceType = cancelOrderDeliveryVO.getCancelFlowOrderDTO().getServicetype().equals(ServiceTypeEnum.TAKE_TYPE.getValue()) ? 1 : 2;
        cancelOrderDelivery(cancelOrderDeliveryVO.getRenterOrderNo(),serviceType);
        deliveryCarTask.cancelRenYunFlowOrderInfo(cancelOrderDeliveryVO.getCancelFlowOrderDTO());
    }

    /**
     * 插入配送地址/配送订单信息
     * @param orderDeliveryVO
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertDeliveryAddress(OrderDeliveryVO orderDeliveryVO) {
        RenterDeliveryAddrEntity deliveryAddrEntity = new RenterDeliveryAddrEntity();
        BeanUtils.copyProperties(orderDeliveryVO.getRenterDeliveryAddrDTO(), deliveryAddrEntity);
        deliveryAddrMapper.insertSelective(deliveryAddrEntity);
        if (orderDeliveryVO.getOrderDeliveryDTO() != null) {
            RenterOrderDeliveryEntity orderDeliveryEntity = new RenterOrderDeliveryEntity();
            BeanUtils.copyProperties(orderDeliveryVO.getOrderDeliveryDTO(), orderDeliveryEntity);
            orderDeliveryMapper.insertSelective(orderDeliveryEntity);
        }
    }

    /**
     * 取消配送订单
     * @param renterOrderNo
     * @param serviceType
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderDelivery(String renterOrderNo,Integer serviceType) {
        RenterOrderDeliveryEntity orderDeliveryEntity = orderDeliveryMapper.findRenterOrderByRenterOrderNo(renterOrderNo,serviceType);
        if (null == orderDeliveryEntity) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到该配送订单信息");
        }
        orderDeliveryMapper.updateById(orderDeliveryEntity.getId());
    }

    /**
     * 构造配送订单数据
     * @param orderReqContext
     * @return
     */
    public OrderDeliveryVO createOrderDeliveryParams(OrderReqContext orderReqContext) {
        if (null == orderReqContext) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        OrderDeliveryVO orderDeliveryVO = new OrderDeliveryVO();
        RenterDeliveryAddrDTO renterDeliveryAddrDTO = new RenterDeliveryAddrDTO();
        NormalOrderReqVO normalOrderReqVO = orderReqContext.getNormalOrderReqVO();
        OrderDeliveryDTO orderDeliveryDTO = new OrderDeliveryDTO();
        RenterMemberDTO renterMemberDTO = orderReqContext.getRenterMemberDto();
        OwnerMemberDTO ownerMemberDTO = orderReqContext.getOwnerMemberDto();
        RenYunFlowOrderDTO renYunFlowOrderDTO = new RenYunFlowOrderDTO();
        RenterGoodsDetailDTO renterGoodsDetailDTO = orderReqContext.getRenterGoodsDetailDto();
        //不使用还车服务（一定不使用取车服务）
        if (normalOrderReqVO.getSrvReturnFlag().intValue() == UsedDeliveryTypeEnum.NO_USED.getValue().intValue()) {

            String carShowAddr = renterGoodsDetailDTO.getCarShowAddr() == null ? renterGoodsDetailDTO.getCarRealAddr() : renterGoodsDetailDTO.getCarShowAddr();
            String carShowLat = renterGoodsDetailDTO.getCarShowLat() == null ? renterGoodsDetailDTO.getCarRealLat() : renterGoodsDetailDTO.getCarShowLat();
            String carShowLng = renterGoodsDetailDTO.getCarShowLon() == null ? renterGoodsDetailDTO.getCarRealLon() : renterGoodsDetailDTO.getCarShowLon();
            //入库取送地址数据
            if (StringUtils.isNotBlank(renterGoodsDetailDTO.getCarShowAddr())) {
                renterDeliveryAddrDTO.setActGetCarAddr(carShowAddr);
                renterDeliveryAddrDTO.setActGetCarLat(carShowLat);
                renterDeliveryAddrDTO.setActGetCarLon(carShowLng);
                renterDeliveryAddrDTO.setActReturnCarAddr(carShowAddr);
                renterDeliveryAddrDTO.setActReturnCarLat(carShowLat);
                renterDeliveryAddrDTO.setActReturnCarLon(carShowLng);
                renterDeliveryAddrDTO.setExpGetCarAddr(carShowAddr);
                renterDeliveryAddrDTO.setExpGetCarLat(carShowLat);
                renterDeliveryAddrDTO.setExpGetCarLon(carShowLng);
                renterDeliveryAddrDTO.setCreateTime(LocalDateTime.now());
                renterDeliveryAddrDTO.setCreateOp("");
            }
        } else {
            /**组装地址信息**/
            renterDeliveryAddrDTO.setActGetCarAddr(normalOrderReqVO.getSrvGetAddr());
            renterDeliveryAddrDTO.setActGetCarLat(normalOrderReqVO.getSrvGetLat());
            renterDeliveryAddrDTO.setActGetCarLon(normalOrderReqVO.getSrvGetLon());
            renterDeliveryAddrDTO.setActReturnCarAddr(normalOrderReqVO.getSrvReturnAddr());
            renterDeliveryAddrDTO.setActReturnCarLat(normalOrderReqVO.getSrvReturnLat());
            renterDeliveryAddrDTO.setActReturnCarLon(normalOrderReqVO.getSrvReturnLon());
            renterDeliveryAddrDTO.setExpGetCarAddr(normalOrderReqVO.getSrvReturnAddr());
            renterDeliveryAddrDTO.setExpGetCarLat(normalOrderReqVO.getSrvReturnLat());
            renterDeliveryAddrDTO.setExpGetCarLon(normalOrderReqVO.getSrvReturnLon());
            renterDeliveryAddrDTO.setCreateTime(LocalDateTime.now());
            renterDeliveryAddrDTO.setCreateOp("");
            /**组装配送订单信息**/
            orderDeliveryDTO.setCityCode(normalOrderReqVO.getCityCode());
            orderDeliveryDTO.setCityName(normalOrderReqVO.getCityName());
            orderDeliveryDTO.setCreateOp("");
            orderDeliveryDTO.setRenterGetReturnAddr(normalOrderReqVO.getSrvReturnAddr());
            orderDeliveryDTO.setRenterGetReturnAddrLat(normalOrderReqVO.getSrvReturnLat());
            orderDeliveryDTO.setRenterGetReturnAddrLon(normalOrderReqVO.getSrvReturnLon());
            orderDeliveryDTO.setRenterName(renterMemberDTO.getRealName());
            orderDeliveryDTO.setRenterPhone(renterMemberDTO.getPhone());
            orderDeliveryDTO.setOrderNo(renterGoodsDetailDTO.getOrderNo());
            orderDeliveryDTO.setCityCode(normalOrderReqVO.getCityCode());
            orderDeliveryDTO.setIsNotifyRenyun(UsedDeliveryTypeEnum.USED.getValue().intValue());
            orderDeliveryDTO.setOwnerGetReturnAddr(normalOrderReqVO.getSrvReturnAddr());
            orderDeliveryDTO.setOwnerGetReturnAddrLat(normalOrderReqVO.getSrvReturnLat());
            orderDeliveryDTO.setOwnerGetReturnAddrLon(normalOrderReqVO.getSrvReturnLon());
            orderDeliveryDTO.setOwnerName(ownerMemberDTO.getRealName());
            orderDeliveryDTO.setOwnerPhone(ownerMemberDTO.getPhone());
            orderDeliveryDTO.setRenterOrderNo(renterGoodsDetailDTO.getRenterOrderNo());
            orderDeliveryDTO.setRentTime(renterGoodsDetailDTO.getRentTime());
            orderDeliveryDTO.setRevertTime(renterGoodsDetailDTO.getRevertTime());
            if (normalOrderReqVO.getSrvReturnFlag().intValue() == UsedDeliveryTypeEnum.USED.getValue().intValue()) {
                orderDeliveryDTO.setType(2);
                renYunFlowOrderDTO.setAlsocaraddr(orderDeliveryDTO.getRenterGetReturnAddr());
            } else if (normalOrderReqVO.getSrvGetFlag().intValue() == UsedDeliveryTypeEnum.USED.getValue().intValue()) {
                orderDeliveryDTO.setType(1);
                renYunFlowOrderDTO.setPickupcaraddr(orderDeliveryDTO.getRenterGetReturnAddr());
            }
            /**组装仁云信息**/
            renYunFlowOrderDTO.setAfterTime(DateUtils.formate(renterGoodsDetailDTO.getRentTime(), DateUtils.DATE_DEFAUTE_4));
            renYunFlowOrderDTO.setBeforeTime(DateUtils.formate(renterGoodsDetailDTO.getRevertTime(), DateUtils.DATE_DEFAUTE_4));
            renYunFlowOrderDTO.setAlsocaraddr(orderDeliveryDTO.getRenterGetReturnAddr());
            renYunFlowOrderDTO.setCarLat(orderDeliveryDTO.getRenterGetReturnAddrLat());
            renYunFlowOrderDTO.setCarLon(orderDeliveryDTO.getRenterGetReturnAddrLon());
            renYunFlowOrderDTO.setCarno(String.valueOf(renterGoodsDetailDTO.getCarNo()));
        }
        orderDeliveryVO.setOrderDeliveryDTO(orderDeliveryDTO);
        orderDeliveryVO.setRenterDeliveryAddrDTO(renterDeliveryAddrDTO);
        orderDeliveryVO.setRenYunFlowOrderDTO(renYunFlowOrderDTO);
        return orderDeliveryVO;
    }

}
