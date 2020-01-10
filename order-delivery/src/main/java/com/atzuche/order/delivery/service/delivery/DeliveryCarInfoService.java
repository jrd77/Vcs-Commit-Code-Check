package com.atzuche.order.delivery.service.delivery;

import com.atzuche.order.delivery.entity.*;
import com.atzuche.order.delivery.enums.HandoverCarTypeEnum;
import com.atzuche.order.delivery.service.RenterOrderDeliveryService;
import com.atzuche.order.delivery.service.handover.HandoverCarService;
import com.atzuche.order.delivery.service.handover.OwnerHandoverCarService;
import com.atzuche.order.delivery.service.handover.RenterHandoverCarService;
import com.atzuche.order.delivery.utils.CommonUtil;
import com.atzuche.order.delivery.utils.DateUtils;
import com.atzuche.order.delivery.utils.MathUtil;
import com.atzuche.order.delivery.vo.delivery.rep.*;
import com.atzuche.order.delivery.vo.delivery.req.DeliveryCarRepVO;
import com.autoyol.platformcost.CommonUtils;
import com.autoyol.platformcost.OwnerFeeCalculatorUtils;
import com.autoyol.platformcost.RenterFeeCalculatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author 胡春林
 * 配送信息服务
 */
@Service
@Slf4j
public class DeliveryCarInfoService {

    @Autowired
    DeliveryCarService deliveryCarService;
    @Autowired
    RenterOrderDeliveryService renterOrderDeliveryService;
    @Autowired
    HandoverCarService handoverCarService;
    @Autowired
    DeliveryCarInfoPriceService deliveryCarInfoPriceService;
    @Autowired
    RenterHandoverCarService renterHandoverCarService;
    @Autowired
    OwnerHandoverCarService ownerHandoverCarService;

    /**
     * 获取配送相关信息
     *
     * @param deliveryCarDTO
     * @return
     */
    public DeliveryCarVO findDeliveryListByOrderNo(DeliveryCarRepVO deliveryCarDTO, OwnerGetAndReturnCarDTO ownerGetAndReturnCarDTO, Boolean isEscrowCar,Integer carEngineType,int carType) {
        List<RenterHandoverCarInfoEntity> renterHandoverCarInfoEntities = renterHandoverCarService.selectRenterByOrderNo(deliveryCarDTO.getOrderNo());
        List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoEntities = ownerHandoverCarService.selectOwnerByOrderNo(deliveryCarDTO.getOrderNo());
        List<RenterHandoverCarRemarkEntity> renterHandoverCarRemarkEntities = renterHandoverCarService.getRenterHandoverRemarkInfo(deliveryCarDTO.getOrderNo());
        List<OwnerHandoverCarRemarkEntity> ownerHandoverCarRemarkEntities = ownerHandoverCarService.getOwnerHandoverRemarkInfo(deliveryCarDTO.getOrderNo());
        List<RenterOrderDeliveryEntity> renterOrderDeliveryEntityList = renterOrderDeliveryService.listRenterOrderDeliveryByRenterOrderNo(deliveryCarDTO.getRenterOrderNo());
        DeliveryCarVO deliveryCarVO = createDeliveryCarVOParams(ownerGetAndReturnCarDTO, renterHandoverCarInfoEntities, ownerHandoverCarInfoEntities, renterHandoverCarRemarkEntities, ownerHandoverCarRemarkEntities, renterOrderDeliveryEntityList, isEscrowCar, carEngineType, carType);
        return deliveryCarVO;
    }


    /**
     * 构造结构体
     *
     * @param renterHandoverCarInfoEntities
     * @param ownerHandoverCarInfoEntities
     * @param renterHandoverCarRemarkEntities
     * @param ownerHandoverCarRemarkEntities
     * @return
     */
    public DeliveryCarVO createDeliveryCarVOParams(OwnerGetAndReturnCarDTO ownerGetAndReturnCarDTO,
                                                   List<RenterHandoverCarInfoEntity> renterHandoverCarInfoEntities,
                                                   List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoEntities,
                                                   List<RenterHandoverCarRemarkEntity> renterHandoverCarRemarkEntities,
                                                   List<OwnerHandoverCarRemarkEntity> ownerHandoverCarRemarkEntities,
                                                   List<RenterOrderDeliveryEntity> renterOrderDeliveryEntityList,
                                                   Boolean isEscrowCar,
                                                   Integer carEngineType,
                                                   Integer carType) {
        DeliveryCarVO deliveryCarVO = new DeliveryCarVO();
        deliveryCarVO.setIsReturnCar(0);
        deliveryCarVO.setIsGetCar(0);
        for (RenterOrderDeliveryEntity renterOrderDeliveryEntity : renterOrderDeliveryEntityList) {
            if (null == renterOrderDeliveryEntity) {
                continue;
            }
            createGetHandoverCar(deliveryCarVO, renterHandoverCarRemarkEntities, ownerHandoverCarRemarkEntities, renterOrderDeliveryEntity,carType);
        }
        //取车时的所在城市
        String cityCode = renterOrderDeliveryEntityList.stream().filter(r->r.getType() == 1).map(RenterOrderDeliveryEntity::getCityCode).findFirst().get();
        deliveryCarVO = createDeliveryCarInfo(ownerGetAndReturnCarDTO, deliveryCarVO, ownerHandoverCarInfoEntities, renterHandoverCarInfoEntities, isEscrowCar,carEngineType,cityCode);
        return deliveryCarVO;
    }

    /**
     * 组装取送车信息
     *
     * @param renterHandoverCarRemarkEntities
     * @param ownerHandoverCarRemarkEntities
     * @param renterOrderDeliveryEntity
     * @return
     */
    public void createGetHandoverCar(DeliveryCarVO deliveryCarVO,
                                     List<RenterHandoverCarRemarkEntity> renterHandoverCarRemarkEntities,
                                     List<OwnerHandoverCarRemarkEntity> ownerHandoverCarRemarkEntities,
                                     RenterOrderDeliveryEntity renterOrderDeliveryEntity,
                                     Integer carType) {
        if (renterOrderDeliveryEntity.getType() == 1 && renterOrderDeliveryEntity.getStatus() != 0) {
            GetHandoverCarDTO getHandoverCarDTO = new GetHandoverCarDTO();
            getHandoverCarDTO = getHandoverCarInfo(getHandoverCarDTO,renterOrderDeliveryEntity,carType);
            String remark = ownerHandoverCarRemarkEntities.stream().filter(r -> (r.getType().intValue() != HandoverCarTypeEnum.RENYUN_TO_RENTER.getValue().intValue())).findFirst().get().getRemark();
            String renterRemark = renterHandoverCarRemarkEntities.stream().filter(r -> (r.getType().intValue() != HandoverCarTypeEnum.RENYUN_TO_RENTER.getValue().intValue())).findFirst().get().getRemark();
            getHandoverCarDTO.setRenterRealGetAddrReamrk(renterRemark);
            getHandoverCarDTO.setOwnRealGetRemark(remark);
            deliveryCarVO.setGetHandoverCarDTO(getHandoverCarDTO);
            deliveryCarVO.setIsGetCar(1);
        } else if (renterOrderDeliveryEntity.getType() == 2 && renterOrderDeliveryEntity.getStatus() != 0) {
            ReturnHandoverCarDTO returnHandoverCarDTO = new ReturnHandoverCarDTO();
            returnHandoverCarDTO = returnHandoverCarInfo(returnHandoverCarDTO,renterOrderDeliveryEntity,carType);
            String remark = ownerHandoverCarRemarkEntities.stream().filter(r -> (r.getType().intValue() != HandoverCarTypeEnum.RENTER_TO_RENYUN.getValue().intValue())).findFirst().get().getRemark();
            String renterRemark = renterHandoverCarRemarkEntities.stream().filter(r -> (r.getType().intValue() != HandoverCarTypeEnum.RENTER_TO_RENYUN.getValue().intValue())).findFirst().get().getRemark();
            returnHandoverCarDTO.setRenterRealGetRemark(renterRemark);
            returnHandoverCarDTO.setOwnerRealGetAddrReamrk(remark);
            deliveryCarVO.setReturnHandoverCarDTO(returnHandoverCarDTO);
            deliveryCarVO.setIsReturnCar(1);
        }
    }

    /**
     * 构造最终数据
     * @param deliveryCarVO
     * @param ownerHandoverCarInfoEntities
     * @param renterHandoverCarInfoEntities
     * @return
     */
    public DeliveryCarVO createDeliveryCarInfo(OwnerGetAndReturnCarDTO ownerGetAndReturnCarDTO, DeliveryCarVO deliveryCarVO, List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoEntities, List<RenterHandoverCarInfoEntity> renterHandoverCarInfoEntities, Boolean isEscrowCar,Integer carEngineType,String cityCode) {
        RenterGetAndReturnCarDTO renterGetAndReturnCarDTO = RenterGetAndReturnCarDTO.builder().build();
        //车主取送信息
        ownerGetAndReturnCarDTO = createOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDTO, ownerHandoverCarInfoEntities,carEngineType,cityCode);
        //租客取送信息
        ownerHandoverCarInfoEntities = CommonUtil.copyList(renterHandoverCarInfoEntities);
        OwnerGetAndReturnCarDTO getAndReturnCarDTO = createOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDTO, ownerHandoverCarInfoEntities,carEngineType,cityCode);
        BeanUtils.copyProperties(getAndReturnCarDTO, renterGetAndReturnCarDTO);
        ownerGetAndReturnCarDTO.setPlatFormOilServiceCharge(RenterFeeCalculatorUtils.calServiceChargeFee().getTotalFee().toString());
        renterGetAndReturnCarDTO.setCarOwnerOilCrash("0");
        if (isEscrowCar) {
            ownerGetAndReturnCarDTO.setCarOilDifferenceCrash("0");
            ownerGetAndReturnCarDTO.setCarOilServiceCharge("0");
            ownerGetAndReturnCarDTO.setCarOwnerOilCrash("0");
        }
        deliveryCarVO.setOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDTO);
        deliveryCarVO.setRenterGetAndReturnCarDTO(renterGetAndReturnCarDTO);
        return deliveryCarVO;
    }


    /**
     * 获取车主租客取送车信息
     *
     * @param ownerGetAndReturnCarDTO
     * @param HandoverCarInfoEntities
     * @return
     */
    public OwnerGetAndReturnCarDTO createOwnerGetAndReturnCarDTO(OwnerGetAndReturnCarDTO ownerGetAndReturnCarDTO, List<OwnerHandoverCarInfoEntity> HandoverCarInfoEntities,Integer carEngineType,String cityCode) {
        for (OwnerHandoverCarInfoEntity ownerHandoverCarInfoEntity : HandoverCarInfoEntities) {
            if (Objects.isNull(ownerHandoverCarInfoEntity.getType())) {
                continue;
            }
            if (ownerHandoverCarInfoEntity.getType().intValue() == HandoverCarTypeEnum.RENYUN_TO_RENTER.getValue().intValue()) {
                ownerGetAndReturnCarDTO.setGetCarOil(String.valueOf(ownerHandoverCarInfoEntity.getOilNum()));
                ownerGetAndReturnCarDTO.setGetKM(String.valueOf(ownerHandoverCarInfoEntity.getMileageNum()));
                ownerGetAndReturnCarDTO.setRealGetTime(String.valueOf(ownerHandoverCarInfoEntity.getRealReturnTime()));
            } else {
                ownerGetAndReturnCarDTO.setReturnCarOil(String.valueOf(ownerHandoverCarInfoEntity.getOilNum()));
                ownerGetAndReturnCarDTO.setReturnKM(String.valueOf(ownerHandoverCarInfoEntity.getMileageNum()));
                ownerGetAndReturnCarDTO.setRealReturnTime(String.valueOf(ownerHandoverCarInfoEntity.getRealReturnTime()));
            }
        }
        //行驶里程
        String ownerDrivingKM = String.valueOf(Math.abs(Integer.valueOf(ownerGetAndReturnCarDTO.getKM)) - Math.abs(Integer.valueOf(ownerGetAndReturnCarDTO.getReturnKM())));
        int oilDifference = Math.abs(Integer.valueOf(ownerGetAndReturnCarDTO.getCarOil)) - Math.abs(Integer.valueOf(ownerGetAndReturnCarDTO.getReturnCarOil()));
        ownerGetAndReturnCarDTO.setDrivingKM(ownerDrivingKM);
        ownerGetAndReturnCarDTO.setOilDifference(String.valueOf(oilDifference));
        ownerGetAndReturnCarDTO.setOilDifferenceCrash(String.valueOf(MathUtil.mul(oilDifference,deliveryCarInfoPriceService.getOilPriceByCityCodeAndType(Integer.valueOf(cityCode),carEngineType))));
        ownerGetAndReturnCarDTO.setOilServiceCharge("0");
        return ownerGetAndReturnCarDTO;
    }

    /**
     * 获取取/还车实际距离
     *
     * @param renterOrderDeliveryEntity
     * @return
     */
    public Double getDistanceKM(RenterOrderDeliveryEntity renterOrderDeliveryEntity) {
        try {
            double carLat = Double.valueOf(renterOrderDeliveryEntity.getRenterGetReturnAddrLat()).doubleValue();
            double carLng = Double.valueOf(renterOrderDeliveryEntity.getRenterGetReturnAddrLon()).doubleValue();
            double originCarLon = Double.valueOf(renterOrderDeliveryEntity.getOwnerGetReturnAddrLat()).doubleValue();
            double originCarLat = Double.valueOf(renterOrderDeliveryEntity.getOwnerGetReturnAddrLon()).doubleValue();
            return CommonUtils.calcDistance(carLat, carLng, originCarLon, originCarLat);
        } catch (Exception e) {
            log.info("换算取还车距离失败，cause:{}", e.getMessage());
            return 0d;
        }
    }

    /**
     * 获取取车相关数据
     * @return
     */
    public GetHandoverCarDTO getHandoverCarInfo(GetHandoverCarDTO getHandoverCarDTO, RenterOrderDeliveryEntity renterOrderDeliveryEntity, Integer carType) {
        getHandoverCarDTO.setChaoYunNengAddCrash("0");
        getHandoverCarDTO.setIsChaoYunNeng("0");
        getHandoverCarDTO.setGetCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvGetAmt(carType, renterOrderDeliveryEntity.getType())));
        getHandoverCarDTO.setGetCarKM(String.valueOf(getDistanceKM(renterOrderDeliveryEntity)));
        getHandoverCarDTO.setOwnerGetCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvGetAmt(carType, renterOrderDeliveryEntity.getType())));
        getHandoverCarDTO.setRenterRealGetAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        getHandoverCarDTO.setOwnDefaultGetCarAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
        getHandoverCarDTO.setOwnRealReturnAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        LocalDateTime rentTime = renterOrderDeliveryEntity.getRentTime();
        getHandoverCarDTO.setRentTime(DateUtils.formate(rentTime, DateUtils.DATE_DEFAUTE_4) + "," + renterOrderDeliveryEntity.getAheadOrDelayTime());
        return getHandoverCarDTO;
    }

    /**
     * 获取还车相关数据
     * @return
     */
    public ReturnHandoverCarDTO returnHandoverCarInfo(ReturnHandoverCarDTO returnHandoverCarDTO, RenterOrderDeliveryEntity renterOrderDeliveryEntity, Integer carType) {
        returnHandoverCarDTO.setChaoYunNengAddCrash("0");
        returnHandoverCarDTO.setIsChaoYunNeng("0");
        returnHandoverCarDTO.setReturnCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvReturnAmt(carType, renterOrderDeliveryEntity.getType())));
        returnHandoverCarDTO.setReturnCarKM(String.valueOf(getDistanceKM(renterOrderDeliveryEntity)));
        returnHandoverCarDTO.setOwnerReturnCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvReturnAmt(carType, renterOrderDeliveryEntity.getType())));
        returnHandoverCarDTO.setRenterRealReturnAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        returnHandoverCarDTO.setOwnDefaultReturnCarAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
        returnHandoverCarDTO.setOwnerRealGetAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        LocalDateTime rentTime = renterOrderDeliveryEntity.getRentTime();
        returnHandoverCarDTO.setRentTime(DateUtils.formate(rentTime, DateUtils.DATE_DEFAUTE_4) + "," + renterOrderDeliveryEntity.getAheadOrDelayTime());
        return  returnHandoverCarDTO;
    }

}
