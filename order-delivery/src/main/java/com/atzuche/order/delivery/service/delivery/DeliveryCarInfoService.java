package com.atzuche.order.delivery.service.delivery;

import com.atzuche.order.commons.StringUtil;
import com.atzuche.order.commons.entity.dto.CostBaseDTO;
import com.atzuche.order.commons.entity.dto.GetReturnCarOverCostReqDto;
import com.atzuche.order.delivery.common.delivery.TranSportService;
import com.atzuche.order.delivery.common.delivery.dto.GetReturnOverCostDTO;
import com.atzuche.order.delivery.entity.*;
import com.atzuche.order.delivery.enums.RenterHandoverCarTypeEnum;
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
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
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
    @Autowired
    TranSportService tranSportService;

    /**
     * 获取配送相关信息
     * @param deliveryCarDTO
     * @return
     */
    public DeliveryCarVO findDeliveryListByOrderNo(String renterOrderNo,DeliveryCarRepVO deliveryCarDTO, OwnerGetAndReturnCarDTO ownerGetAndReturnCarDTO, Boolean isEscrowCar,Integer carEngineType,int carType) {
        List<RenterHandoverCarInfoEntity> renterHandoverCarInfoEntities = renterHandoverCarService.selectRenterByOrderNo(deliveryCarDTO.getOrderNo());
        List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoEntities = ownerHandoverCarService.selectOwnerByOrderNo(deliveryCarDTO.getOrderNo());
        List<RenterHandoverCarRemarkEntity> renterHandoverCarRemarkEntities = renterHandoverCarService.getRenterHandoverRemarkInfo(deliveryCarDTO.getOrderNo());
        List<OwnerHandoverCarRemarkEntity> ownerHandoverCarRemarkEntities = ownerHandoverCarService.getOwnerHandoverRemarkInfo(deliveryCarDTO.getOrderNo());
        List<RenterOrderDeliveryEntity> renterOrderDeliveryEntityList = renterOrderDeliveryService.listRenterOrderDeliveryByRenterOrderNo(renterOrderNo);
        DeliveryCarVO deliveryCarVO = createDeliveryCarVOParams(ownerGetAndReturnCarDTO, renterHandoverCarInfoEntities, ownerHandoverCarInfoEntities, renterHandoverCarRemarkEntities, ownerHandoverCarRemarkEntities, renterOrderDeliveryEntityList, isEscrowCar, carEngineType, carType);
        return deliveryCarVO;
    }


    /**
     * 构造结构体
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
        RenterOrderDeliveryEntity renterOrderDelivery = renterOrderDeliveryEntityList.stream().filter(r->r.getType() == 1).findFirst().get();
        String cityCode = renterOrderDelivery.getCityCode();
        String tenancy = String.valueOf(Duration.between(renterOrderDelivery.getRentTime(),renterOrderDelivery.getRevertTime()).toDays());
        ownerGetAndReturnCarDTO.setZuQi(tenancy);
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

        //获取超运能
        GetReturnCarOverCostReqDto getReturnCarOverCostReqDto = new GetReturnCarOverCostReqDto();
        getReturnCarOverCostReqDto.setCityCode(Integer.valueOf(renterOrderDeliveryEntity.getCityCode()));
        CostBaseDTO costBaseDTO = new CostBaseDTO();
        costBaseDTO.setStartTime(renterOrderDeliveryEntity.getRentTime());
        costBaseDTO.setEndTime(renterOrderDeliveryEntity.getRevertTime());
        getReturnCarOverCostReqDto.setCostBaseDTO(costBaseDTO);
        String isGetOverTransport;
        String isReturnOverTransport;
        try {
            GetReturnOverCostDTO getReturnOverCostDTO = tranSportService.getGetReturnOverCost(getReturnCarOverCostReqDto);
            isGetOverTransport = getReturnOverCostDTO.getGetReturnOverTransportDTO().getIsGetOverTransport() == true ? "1" : "0";
            isReturnOverTransport = getReturnOverCostDTO.getGetReturnOverTransportDTO().getIsReturnOverTransport() == true ? "1" : "0";
        } catch (Exception e) {
            log.error("获取超运能异常，给默认值,cause:{}", e.getMessage());
            isGetOverTransport = "0";
            isReturnOverTransport = "0";
        }
        if (renterOrderDeliveryEntity.getType() == 1 && renterOrderDeliveryEntity.getStatus() != 0) {
            GetHandoverCarDTO getHandoverCarDTO = new GetHandoverCarDTO();
            getHandoverCarDTO = getHandoverCarInfo(getHandoverCarDTO, renterOrderDeliveryEntity, carType);
            try {
                getHandoverCarDTO.setRenterRealGetAddrReamrk(renterOrderDeliveryEntity.getRenterRealGetReturnRemark());
                getHandoverCarDTO.setOwnRealGetRemark(renterOrderDeliveryEntity.getOwnerRealGetReturnRemark());
            } catch (Exception e) {
                log.error("备注获取失败");
            }
            getHandoverCarDTO.setIsChaoYunNeng(isGetOverTransport);
            deliveryCarVO.setGetHandoverCarDTO(getHandoverCarDTO);
            deliveryCarVO.setIsGetCar(1);
        } else if (renterOrderDeliveryEntity.getType() == 2 && renterOrderDeliveryEntity.getStatus() != 0) {
            ReturnHandoverCarDTO returnHandoverCarDTO = new ReturnHandoverCarDTO();
            returnHandoverCarDTO = returnHandoverCarInfo(returnHandoverCarDTO, renterOrderDeliveryEntity, carType);
            try {
                returnHandoverCarDTO.setRenterRealGetRemark(renterOrderDeliveryEntity.getRenterRealGetReturnRemark());
                returnHandoverCarDTO.setOwnerRealGetAddrReamrk(renterOrderDeliveryEntity.getOwnerRealGetReturnRemark());
            } catch (Exception e) {
                log.error("备注获取失败");
            }
            returnHandoverCarDTO.setIsChaoYunNeng(isReturnOverTransport);
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
        ownerGetAndReturnCarDTO = deliveryCarInfoPriceService.createOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDTO, ownerHandoverCarInfoEntities,carEngineType,cityCode);
        OwnerGetAndReturnCarDTO ownerGetAndReturnCarDO = OwnerGetAndReturnCarDTO.builder().build();
        BeanUtils.copyProperties(ownerGetAndReturnCarDTO,ownerGetAndReturnCarDO);
        //租客取送信息
        List<OwnerHandoverCarInfoEntity> ownerHandoverCarInfoList = Lists.newArrayList();
        for(RenterHandoverCarInfoEntity renterGetAndReturnCar : renterHandoverCarInfoEntities )
        {
            OwnerHandoverCarInfoEntity ownerGetAndReturnCar = new OwnerHandoverCarInfoEntity();
            BeanUtils.copyProperties(renterGetAndReturnCar,ownerGetAndReturnCar);
            ownerHandoverCarInfoList.add(ownerGetAndReturnCar);
        }
        ownerHandoverCarInfoEntities = CommonUtil.copyList(ownerHandoverCarInfoList);
        OwnerGetAndReturnCarDTO getAndReturnCarDTO = deliveryCarInfoPriceService.createOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDO, ownerHandoverCarInfoEntities,carEngineType,cityCode);
        BeanUtils.copyProperties(getAndReturnCarDTO, renterGetAndReturnCarDTO);
        ownerGetAndReturnCarDTO.setPlatFormOilServiceCharge(RenterFeeCalculatorUtils.calServiceChargeFee().getTotalFee().toString());
        if(org.apache.commons.lang3.StringUtils.isNotBlank(ownerGetAndReturnCarDTO.getCarOilDifferenceCrash())) {
            renterGetAndReturnCarDTO.setCarOwnerOilCrash(ownerGetAndReturnCarDTO.getCarOilDifferenceCrash());
        }
        if (isEscrowCar) {
            ownerGetAndReturnCarDTO.setCarOilDifferenceCrash(ownerGetAndReturnCarDTO.getOilDifferenceCrash());
            ownerGetAndReturnCarDTO.setCarOwnerOilCrash(ownerGetAndReturnCarDTO.getOilDifferenceCrash());
        }
        deliveryCarVO.setOwnerGetAndReturnCarDTO(ownerGetAndReturnCarDTO);
        deliveryCarVO.setRenterGetAndReturnCarDTO(renterGetAndReturnCarDTO);
        return deliveryCarVO;
    }


    /**
     * 获取取车相关数据
     * @return
     */
    public GetHandoverCarDTO getHandoverCarInfo(GetHandoverCarDTO getHandoverCarDTO, RenterOrderDeliveryEntity renterOrderDeliveryEntity, Integer carType) {
        getHandoverCarDTO.setChaoYunNengAddCrash("0");
        getHandoverCarDTO.setGetCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvGetAmt(carType, renterOrderDeliveryEntity.getType())));
        getHandoverCarDTO.setGetCarKM(String.valueOf(deliveryCarInfoPriceService.getDistanceKM(renterOrderDeliveryEntity)));
        getHandoverCarDTO.setOwnerGetCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvGetAmt(carType, renterOrderDeliveryEntity.getType())));
        getHandoverCarDTO.setRenterRealGetAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        getHandoverCarDTO.setOwnDefaultGetCarAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
        getHandoverCarDTO.setOwnRealReturnAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
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
        returnHandoverCarDTO.setReturnCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvReturnAmt(carType, renterOrderDeliveryEntity.getType())));
        returnHandoverCarDTO.setReturnCarKM(String.valueOf(deliveryCarInfoPriceService.getDistanceKM(renterOrderDeliveryEntity)));
        returnHandoverCarDTO.setOwnerReturnCarCrash(String.valueOf(OwnerFeeCalculatorUtils.calOwnerSrvReturnAmt(carType, renterOrderDeliveryEntity.getType())));
        returnHandoverCarDTO.setRenterRealReturnAddr(renterOrderDeliveryEntity.getRenterGetReturnAddr());
        returnHandoverCarDTO.setOwnDefaultReturnCarAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
        returnHandoverCarDTO.setOwnerRealGetAddr(renterOrderDeliveryEntity.getOwnerGetReturnAddr());
        LocalDateTime rentTime = renterOrderDeliveryEntity.getRentTime();
        returnHandoverCarDTO.setRentTime(DateUtils.formate(rentTime, DateUtils.DATE_DEFAUTE_4) + "," + renterOrderDeliveryEntity.getAheadOrDelayTime());
        return  returnHandoverCarDTO;
    }

}
