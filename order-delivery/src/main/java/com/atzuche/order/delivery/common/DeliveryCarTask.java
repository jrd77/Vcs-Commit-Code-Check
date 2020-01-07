package com.atzuche.order.delivery.common;

import com.atzuche.order.delivery.entity.RenterDeliveryAddrEntity;
import com.atzuche.order.delivery.entity.RenterOrderDeliveryEntity;
import com.atzuche.order.delivery.enums.DeliveryTypeEnum;
import com.atzuche.order.delivery.enums.HandoverCarTypeEnum;
import com.atzuche.order.delivery.enums.ServiceTypeEnum;
import com.atzuche.order.delivery.enums.UserTypeEnum;
import com.atzuche.order.delivery.exception.DeliveryOrderException;
import com.atzuche.order.delivery.mapper.RenterDeliveryAddrMapper;
import com.atzuche.order.delivery.mapper.RenterOrderDeliveryMapper;
import com.atzuche.order.delivery.service.MailSendService;
import com.atzuche.order.delivery.service.delivery.RenYunDeliveryCarService;
import com.atzuche.order.delivery.service.handover.HandoverCarService;
import com.atzuche.order.delivery.utils.CodeUtils;
import com.atzuche.order.delivery.utils.CommonUtil;
import com.atzuche.order.delivery.utils.EmailConstants;
import com.atzuche.order.delivery.vo.delivery.CancelFlowOrderDTO;
import com.atzuche.order.delivery.vo.delivery.OrderDeliveryVO;
import com.atzuche.order.delivery.vo.delivery.RenYunFlowOrderDTO;
import com.atzuche.order.delivery.vo.delivery.UpdateFlowOrderDTO;
import com.atzuche.order.delivery.vo.handover.HandoverCarInfoDTO;
import com.atzuche.order.delivery.vo.handover.HandoverCarVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author 胡春林
 * 执行流程
 */
@Service
@Slf4j
public class DeliveryCarTask {

    @Autowired
    RenYunDeliveryCarService renyunDeliveryCarService;
    @Autowired
    MailSendService mailSendService;
    @Resource
    RenterDeliveryAddrMapper deliveryAddrMapper;
    @Resource
    RenterOrderDeliveryMapper orderDeliveryMapper;
    @Autowired
    HandoverCarService handoverCarService;
    @Autowired
    CodeUtils codeUtils;

    /**
     * 添加订单到仁云流程系统
     */
    @Async
    public void addRenYunFlowOrderInfo(RenYunFlowOrderDTO renYunFlowOrderDTO) {
        String result = renyunDeliveryCarService.addRenYunFlowOrderInfo(renYunFlowOrderDTO);
        if (StringUtils.isBlank(result)) {
            sendMailByType(renYunFlowOrderDTO.getServicetype(), DeliveryConstants.ADD_TYPE, DeliveryConstants.ADD_FLOW_ORDER, renYunFlowOrderDTO.getOrdernumber());
        }
    }

    /**
     * 更新订单到仁云流程系统
     */
    @Async
    public void updateRenYunFlowOrderInfo(UpdateFlowOrderDTO updateFlowOrderDTO) {
        String result = renyunDeliveryCarService.updateRenYunFlowOrderInfo(updateFlowOrderDTO);
        if (StringUtils.isBlank(result)) {
            sendMailByType(updateFlowOrderDTO.getServicetype(), DeliveryConstants.CHANGE_TYPE, DeliveryConstants.CHANGE_FLOW_ORDER, updateFlowOrderDTO.getOrdernumber());
        }
    }

    /**
     * 取消订单到仁云流程系统
     */
    @Async
    public void cancelRenYunFlowOrderInfo(CancelFlowOrderDTO cancelFlowOrderDTO) {
        String result = renyunDeliveryCarService.cancelRenYunFlowOrderInfo(cancelFlowOrderDTO);
        if (StringUtils.isBlank(result)) {
            sendMailByType(cancelFlowOrderDTO.getServicetype(), DeliveryConstants.CANCEL_TYPE, DeliveryConstants.CANCEL_FLOW_ORDER, cancelFlowOrderDTO.getOrdernumber());
        }
    }

    /**
     * 插入配送地址/配送订单信息
     *
     * @param orderDeliveryVO
     */
    @Transactional(rollbackFor = Exception.class)
    public void insertDeliveryAddress(Integer getMinutes, Integer returnMinutes, OrderDeliveryVO orderDeliveryVO, Integer type) {

        if (orderDeliveryVO.getRenterDeliveryAddrDTO() != null) {
            RenterDeliveryAddrEntity deliveryAddrEntity = new RenterDeliveryAddrEntity();
            BeanUtils.copyProperties(orderDeliveryVO.getRenterDeliveryAddrDTO(), deliveryAddrEntity);
            RenterDeliveryAddrEntity renterDeliveryAddrEntity = deliveryAddrMapper.selectByRenterOrderNo(deliveryAddrEntity.getRenterOrderNo());
            if (null == renterDeliveryAddrEntity) {
                deliveryAddrMapper.insertSelective(deliveryAddrEntity);
            }else {
                CommonUtil.copyPropertiesIgnoreNull(deliveryAddrEntity,renterDeliveryAddrEntity);
                deliveryAddrMapper.updateByPrimaryKey(renterDeliveryAddrEntity);
            }
        }
        if (orderDeliveryVO.getOrderDeliveryDTO() != null) {
            RenterOrderDeliveryEntity orderDeliveryEntity = new RenterOrderDeliveryEntity();
            BeanUtils.copyProperties(orderDeliveryVO.getOrderDeliveryDTO(), orderDeliveryEntity);
            if (type == DeliveryTypeEnum.ADD_TYPE.getValue().intValue()) {
                orderDeliveryEntity.setOrderNoDelivery(codeUtils.createDeliveryNumber());
                if (Objects.isNull(getMinutes) && Objects.isNull(returnMinutes)) {
                    orderDeliveryEntity.setAheadOrDelayTime(0);
                } else {
                    int aheadOrDelayTime = getMinutes == null ? returnMinutes : getMinutes;
                    orderDeliveryEntity.setAheadOrDelayTime(aheadOrDelayTime);
                }
                orderDeliveryEntity.setStatus(1);
                orderDeliveryMapper.insertSelective(orderDeliveryEntity);
                addHandoverCarInfo(orderDeliveryEntity, getMinutes, returnMinutes);
            } else {
                RenterOrderDeliveryEntity lastOrderDeliveryEntity = orderDeliveryMapper.findRenterOrderByrOrderNo(orderDeliveryEntity.getOrderNo(), orderDeliveryEntity.getType());
                if (null == lastOrderDeliveryEntity) {
                    throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_MOUDLE_ERROR.getValue(), "没有找到最近的一笔配送订单记录");
                }
                CommonUtil.copyPropertiesIgnoreNull(orderDeliveryEntity, lastOrderDeliveryEntity);
                lastOrderDeliveryEntity.setStatus(2);
                orderDeliveryMapper.insert(lastOrderDeliveryEntity);
            }
        }
    }

    public void addHandoverCarInfo(RenterOrderDeliveryEntity orderDeliveryEntity, Integer getMinutes, Integer returnMinutes) {

        //提前或延后时间(取车:提前时间, 还车：延后时间
        HandoverCarInfoDTO handoverCarInfoDTO = new HandoverCarInfoDTO();
        handoverCarInfoDTO.setCreateOp("");
        handoverCarInfoDTO.setOrderNo(orderDeliveryEntity.getOrderNo());
        handoverCarInfoDTO.setRenterOrderNo(orderDeliveryEntity.getRenterOrderNo());
        if (getMinutes != null) {
            handoverCarInfoDTO.setAheadTime(getMinutes);
            handoverCarInfoDTO.setType(HandoverCarTypeEnum.RENYUN_TO_RENTER.getValue().intValue());
        } else if (returnMinutes != null) {
            handoverCarInfoDTO.setDelayTime(returnMinutes);
            handoverCarInfoDTO.setType(HandoverCarTypeEnum.RENTER_TO_RENYUN.getValue().intValue());
        }
        HandoverCarVO handoverCarVO = new HandoverCarVO();
        handoverCarVO.setHandoverCarInfoDTO(handoverCarInfoDTO);
        handoverCarService.addHandoverCarInfo(handoverCarVO, UserTypeEnum.RENTER_TYPE.getValue().intValue());
    }

    /**
     * 取消配送订单
     *
     * @param renterOrderNo
     * @param serviceType
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderDelivery(String renterOrderNo, Integer serviceType) {
        RenterOrderDeliveryEntity orderDeliveryEntity = orderDeliveryMapper.findRenterOrderByRenterOrderNo(renterOrderNo, serviceType);
        if (null == orderDeliveryEntity) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到该配送订单信息");
        }
        orderDeliveryMapper.updateStatusById(orderDeliveryEntity.getId());
    }

    /**
     * 发送email
     */
    public void sendMailByType(String serviceType, String actionType, String url, String orderNumber) {
        try {
            String typeName = ServiceTypeEnum.TAKE_TYPE.equals(serviceType) ? DeliveryConstants.SERVICE_TAKE_TEXT : ServiceTypeEnum.BACK_TYPE.equals(serviceType) ? DeliveryConstants.SERVICE_BACK_TEXT : serviceType;
            String interfaceName = "";
            switch (actionType) {
                case DeliveryConstants.ADD_TYPE:
                    interfaceName = DeliveryConstants.ADD_INTERFACE_NAME;
                    break;
                case DeliveryConstants.CHANGE_TYPE:
                    interfaceName = DeliveryConstants.CANCEL_INTERFACE_NAME;
                    break;
                case DeliveryConstants.CANCEL_TYPE:
                    interfaceName = DeliveryConstants.CHANGE_INTERFACE_NAME;
                    break;
                default:
                    break;
            }
            if (mailSendService != null) {
                String[] toEmails = DeliveryConstants.EMAIL_PARAMS.split(",");
                String content = String.format(EmailConstants.PROCESS_SYSTEM_NOTICE_CONTENT, orderNumber, interfaceName, url, typeName);
                mailSendService.sendSimpleEmail(toEmails, EmailConstants.PROCESS_SYSTEM_NOTICE_SUBJECT, content);
            }
        } catch (Exception e) {
            log.info("发送邮件失败---->>>>{}:", e.getMessage());
        }
    }
}