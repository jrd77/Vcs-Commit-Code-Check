package com.atzuche.order.delivery.service.handover;

import com.atzuche.order.delivery.common.DeliveryErrorCode;
import com.atzuche.order.delivery.entity.OwnerHandoverCarInfoEntity;
import com.atzuche.order.delivery.entity.OwnerHandoverCarRemarkEntity;
import com.atzuche.order.delivery.entity.RenterHandoverCarInfoEntity;
import com.atzuche.order.delivery.entity.RenterHandoverCarRemarkEntity;
import com.atzuche.order.delivery.enums.UserTypeEnum;
import com.atzuche.order.delivery.exception.DeliveryOrderException;
import com.atzuche.order.delivery.exception.HandoverCarOrderException;
import com.atzuche.order.delivery.mapper.*;
import com.atzuche.order.delivery.utils.CommonUtil;
import com.atzuche.order.delivery.vo.handover.HandoverCarRepVO;
import com.atzuche.order.delivery.vo.handover.HandoverCarReqVO;
import com.atzuche.order.delivery.vo.handover.HandoverCarVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author 胡春林
 * 交接车服务
 */
@Service
@Slf4j
public class HandoverCarService {

    @Resource
    RenterHandoverCarInfoMapper renterHandoverCarInfoMapper;
    @Resource
    RenterHandoverCarRemarkMapper renterHandoverCarRemarkMapper;
    @Resource
    OwnerHandoverCarInfoMapper ownerHandoverCarInfoMapper;
    @Resource
    OwnerHandoverCarRemarkMapper ownerHandoverCarRemarkMapper;

    /**
     * 新增交接车数据
     *
     * @param handoverCarVO
     */
    @Transactional(rollbackFor = Exception.class)
    public void addHandoverCarInfo(HandoverCarVO handoverCarVO, int userType) {
        if (Objects.isNull(handoverCarVO) || handoverCarVO.getHandoverCarInfoDTO().getType() == null) {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        if (userType == UserTypeEnum.RENTER_TYPE.getValue().intValue()) {
            RenterHandoverCarInfoEntity renterHandoverCarInfoEntity = new RenterHandoverCarInfoEntity();
            BeanUtils.copyProperties(handoverCarVO.getHandoverCarInfoDTO(), renterHandoverCarInfoEntity);
            RenterHandoverCarInfoEntity handoverCarInfoEntity = renterHandoverCarInfoMapper.selectObjectByOrderNo(handoverCarVO.getHandoverCarInfoDTO().getOrderNo(), handoverCarVO.getHandoverCarInfoDTO().getType());
            if (handoverCarInfoEntity != null) {
                CommonUtil.copyPropertiesIgnoreNull(renterHandoverCarInfoEntity, handoverCarInfoEntity);
                renterHandoverCarInfoMapper.updateByPrimaryKey(handoverCarInfoEntity);
            } else {
                renterHandoverCarInfoMapper.insertSelective(renterHandoverCarInfoEntity);
            }
            if (handoverCarVO.getHandoverCarRemarkDTO() != null) {
                RenterHandoverCarRemarkEntity renterHandoverCarRemarkEntity = new RenterHandoverCarRemarkEntity();
                BeanUtils.copyProperties(handoverCarVO.getHandoverCarRemarkDTO(), renterHandoverCarRemarkEntity);
                renterHandoverCarRemarkMapper.insertSelective(renterHandoverCarRemarkEntity);
            }
        } else if (userType == UserTypeEnum.OWNER_TYPE.getValue().intValue()) {
            OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity = new OwnerHandoverCarInfoEntity();
            BeanUtils.copyProperties(handoverCarVO.getHandoverCarInfoDTO(), ownerHandoverCarInfoEntity);
            OwnerHandoverCarInfoEntity handoverCarInfoEntity = ownerHandoverCarInfoMapper.selectObjectByOrderNo(handoverCarVO.getHandoverCarInfoDTO().getOrderNo(), handoverCarVO.getHandoverCarInfoDTO().getType());
            if (handoverCarInfoEntity != null) {
                CommonUtil.copyPropertiesIgnoreNull(ownerHandoverCarInfoEntity, handoverCarInfoEntity);
                ownerHandoverCarInfoMapper.updateByPrimaryKey(handoverCarInfoEntity);
            } else {
                ownerHandoverCarInfoMapper.insertSelective(ownerHandoverCarInfoEntity);
            }
            if (handoverCarVO.getHandoverCarRemarkDTO() != null) {
                OwnerHandoverCarRemarkEntity ownerHandoverCarRemarkEntity = new OwnerHandoverCarRemarkEntity();
                BeanUtils.copyProperties(handoverCarVO.getHandoverCarRemarkDTO(), ownerHandoverCarRemarkEntity);
                ownerHandoverCarRemarkMapper.insertSelective(ownerHandoverCarRemarkEntity);
            }
        } else {
            log.info("没有找到合适的交车类型，handoverCarVO:{}", handoverCarVO.toString());
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到合适的交车类型");
        }
    }

    /**
     * 根据消息ID获取
     *
     * @param msgId
     * @return
     */
    public String getHandoverCarInfoByMsgId(String msgId) {
        if (StringUtils.isBlank(msgId)) {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR);
        }
        String handoverMsgId = renterHandoverCarInfoMapper.queryObjectByMsgId(msgId);
        if (StringUtils.isBlank(handoverMsgId)) {
            handoverMsgId = ownerHandoverCarInfoMapper.queryObjectByMsgId(msgId);
        }
        return handoverMsgId;
    }


    /**
     * 根据子订单号查询(油耗 里程)需要的数据
     *
     * @param handoverCarReqVO
     * @return
     */
    public HandoverCarRepVO getRenterHandover(HandoverCarReqVO handoverCarReqVO) {
        if (null == handoverCarReqVO) {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "参数错误");
        }
        List<RenterHandoverCarInfoEntity> renterHandoverCarInfoEntities = renterHandoverCarInfoMapper.selectByRenterOrderNo(handoverCarReqVO.getRenterOrderNo());
        List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoEntities = ownerHandoverCarInfoMapper.selectObjectByOwnerOrderNo(handoverCarReqVO.getOwnerOrderNo());
        HandoverCarRepVO handoverCarRepVO = new HandoverCarRepVO();
        handoverCarRepVO.setOwnerHandoverCarInfoEntities(ownerHandoverCarInfoEntities);
        handoverCarRepVO.setRenterHandoverCarInfoEntities(renterHandoverCarInfoEntities);
        return handoverCarRepVO;
    }

    /**
     * 更新图片数据
     *
     * @param orderNo
     * @param userType
     * @param photoType
     * @param key
     */
    public void findUpdateHandoverCarInfo(String orderNo, Integer userType, Integer photoType, String key) {
        RenterHandoverCarInfoEntity renterHandoverCarInfoEntity = null;
        OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity = null;
        setHandoverCarInfo(renterHandoverCarInfoEntity, ownerHandoverCarInfoEntity, userType, orderNo, photoType);
        if (renterHandoverCarInfoEntity != null) {
            renterHandoverCarInfoEntity.setImageUrl(key);
            renterHandoverCarInfoMapper.updateByPrimaryKey(renterHandoverCarInfoEntity);
        }
        if (ownerHandoverCarInfoEntity != null) {
            ownerHandoverCarInfoEntity.setImageUrl(key);
            ownerHandoverCarInfoMapper.updateByPrimaryKey(ownerHandoverCarInfoEntity);
        }
    }

    /**
     * 校验订单信息
     *
     * @param memNO
     * @param orderNo
     * @param userType
     * @return
     */
    public boolean validateOrderInfo(Integer memNO, String orderNo, int userType, Integer photoType) {
        RenterHandoverCarInfoEntity renterHandoverCarInfoEntity = null;
        OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity = null;
        setHandoverCarInfo(renterHandoverCarInfoEntity, ownerHandoverCarInfoEntity, userType, orderNo, photoType);
        if (null == renterHandoverCarInfoEntity && Objects.isNull(ownerHandoverCarInfoEntity)) {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到该笔订单记录");
        }
        if (memNO.intValue() != ownerHandoverCarInfoEntity.getRealGetMemNo().intValue() && memNO.intValue() != renterHandoverCarInfoEntity.getRealGetMemNo().intValue()) {
            throw new DeliveryOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "您只能上传自己的取还车照片");
        }
        return true;
    }

    /**
     * 获取租客交接车数据
     *
     * @param orderNo
     * @param type
     * @return
     */
    public RenterHandoverCarInfoEntity getRenterHandoverCarInfo(String orderNo, Integer type) {
        return renterHandoverCarInfoMapper.selectObjectByOrderNo(orderNo, type);
    }

    /**
     * 获取租客交接车数据
     *
     * @param orderNo
     * @return
     */
    public List<RenterHandoverCarInfoEntity> selectRenterByOrderNo(String orderNo) {
        return renterHandoverCarInfoMapper.selectRenterByOrderNo(orderNo);
    }

    /**
     * 获取车主交接车数据
     *
     * @param orderNo
     * @param type
     * @return
     */
    public OwnerHandoverCarInfoEntity getOwnerHandoverCarInfo(String orderNo, Integer type) {

        return ownerHandoverCarInfoMapper.selectObjectByOrderNo(orderNo, type);
    }

    /**
     * 获取車主交接车数据
     *
     * @param orderNo
     * @return
     */
    public List<OwnerHandoverCarInfoEntity> selectOwnerByOrderNo(String orderNo) {
        return ownerHandoverCarInfoMapper.selectOwnerByOrderNo(orderNo);
    }

    /**
     * 获取车主备注信息
     *
     * @param orderNo
     * @return
     */
    public List<OwnerHandoverCarRemarkEntity> getOwnerHandoverRemarkInfo(String orderNo) {

        return ownerHandoverCarRemarkMapper.selectObjectByOrderNo(orderNo);
    }

    /**
     * 获取租客备注信息
     *
     * @param orderNo
     * @return
     */
    public List<RenterHandoverCarRemarkEntity> getRenterHandoverRemarkInfo(String orderNo) {

        return renterHandoverCarRemarkMapper.selectObjectByOrderNo(orderNo);
    }

    /**
     * 更新车主交接车信息
     *
     * @param ownerHandoverCarInfoEntity
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateOwnerHandoverInfo(OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity) {

        return ownerHandoverCarInfoMapper.updateByPrimaryKey(ownerHandoverCarInfoEntity);
    }

    /**
     * 更新租客交接车信息
     *
     * @param renterHandoverCarInfoEntity
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer updateRenterHandoverInfo(RenterHandoverCarInfoEntity renterHandoverCarInfoEntity) {
        return renterHandoverCarInfoMapper.updateByPrimaryKey(renterHandoverCarInfoEntity);
    }

    /**
     * 设置参数
     *
     * @param renterHandoverCarInfoEntity
     * @param ownerHandoverCarInfoEntity
     * @param userType
     * @param orderNo
     * @param photoType
     */
    public void setHandoverCarInfo(RenterHandoverCarInfoEntity renterHandoverCarInfoEntity, OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity, Integer userType, String orderNo, Integer photoType) {
        int type = photoType == 2 ? 3 : 4;
        if (UserTypeEnum.isUserType(userType) && userType == UserTypeEnum.RENTER_TYPE.getValue().intValue()) {
            renterHandoverCarInfoEntity = renterHandoverCarInfoMapper.selectObjectByOrderNo(orderNo, type);
        } else if (UserTypeEnum.isUserType(userType) && userType == UserTypeEnum.OWNER_TYPE.getValue().intValue()) {
            ownerHandoverCarInfoEntity = ownerHandoverCarInfoMapper.selectObjectByOrderNo(orderNo, type);
        } else {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到合适的类型");
        }
        if (null == renterHandoverCarInfoEntity && Objects.isNull(ownerHandoverCarInfoEntity)) {
            throw new HandoverCarOrderException(DeliveryErrorCode.DELIVERY_PARAMS_ERROR.getValue(), "没有找到该笔订单记录");
        }
    }


}
