package com.atzuche.order.rentercost.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.atzuche.order.commons.LocalDateTimeUtils;
import com.atzuche.order.commons.entity.dto.*;
import com.atzuche.order.commons.enums.ChannelNameTypeEnum;
import com.atzuche.order.commons.enums.SubsidySourceCodeEnum;
import com.atzuche.order.commons.enums.SubsidyTypeCodeEnum;
import com.atzuche.order.rentercost.entity.*;
import com.atzuche.order.rentercost.entity.dto.*;
import com.atzuche.order.rentercost.entity.vo.GetReturnResponseVO;
import com.autoyol.commons.utils.GsonUtils;
import com.autoyol.commons.web.ErrorCode;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.atzuche.order.commons.enums.RenterCashCodeEnum;
import com.atzuche.order.rentercost.exception.RenterCostParameterException;
import com.autoyol.platformcost.CommonUtils;
import com.autoyol.platformcost.RenterFeeCalculatorUtils;
import com.autoyol.platformcost.model.CarDepositAmtVO;
import com.autoyol.platformcost.model.CarPriceOfDay;
import com.autoyol.platformcost.model.DepositText;
import com.autoyol.platformcost.model.FeeResult;
import com.autoyol.platformcost.model.IllegalDepositConfig;
import com.autoyol.platformcost.model.InsuranceConfig;
import com.autoyol.platformcost.model.OilAverageCostBO;
import com.dianping.cat.Cat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RenterOrderCostCombineService {
	
	@Autowired
	private RenterOrderCostDetailService renterOrderCostDetailService;
	@Autowired
	private RenterOrderSubsidyDetailService renterOrderSubsidyDetailService;
	@Autowired
	private RenterOrderFineDeatailService renterOrderFineDeatailService;
	@Autowired
	private OrderConsoleCostDetailService orderConsoleCostDetailService;
    @Autowired
    private RestTemplate restTemplate;

    private static final String [] ORDER_TYPES = {"1","3"};
	
	public static final List<RenterCashCodeEnum> RENTERCASHCODEENUM_LIST = new ArrayList<RenterCashCodeEnum>() {

		private static final long serialVersionUID = 1L;

	{
        add(RenterCashCodeEnum.RENT_AMT);
        add(RenterCashCodeEnum.INSURE_TOTAL_PRICES);
        add(RenterCashCodeEnum.ABATEMENT_INSURE);
        add(RenterCashCodeEnum.FEE);
        add(RenterCashCodeEnum.SRV_GET_COST);
        add(RenterCashCodeEnum.SRV_RETURN_COST);
        add(RenterCashCodeEnum.MILEAGE_COST_RENTER);
        add(RenterCashCodeEnum.OIL_COST_RENTER);
        add(RenterCashCodeEnum.EXTRA_DRIVER_INSURE);
    }};

	/**
	 * 获取租金对象列表
	 * @param rentAmtDTO
	 * @return List<RenterOrderCostDetailEntity>
	 */
	public List<RenterOrderCostDetailEntity> listRentAmtEntity(RentAmtDTO rentAmtDTO) {
		log.info("getRentAmtEntity rentAmtDTO=[{}]",rentAmtDTO);
		if (rentAmtDTO == null) {
			log.error("getRentAmtEntity 获取租金对象列表rentAmtDTO对象为空");
			Cat.logError("获取租金对象列表rentAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = rentAmtDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getRentAmtEntity 获取租金对象列表rentAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取租金对象列表rentAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		
		List<RenterGoodsPriceDetailDTO> dayPriceList = rentAmtDTO.getRenterGoodsPriceDetailDTOList();
		// 按还车时间分组
		Map<LocalDateTime, List<RenterGoodsPriceDetailDTO>> dayPriceMap = dayPriceList.stream().collect(Collectors.groupingBy(RenterGoodsPriceDetailDTO::getRevertTime));
		dayPriceMap = dayPriceMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
		int i = 1;
		List<RenterOrderCostDetailEntity> renterOrderCostDetailEntityList = new ArrayList<RenterOrderCostDetailEntity>();
		for(Map.Entry<LocalDateTime, List<RenterGoodsPriceDetailDTO>> it : dayPriceMap.entrySet()){
			if (i == 1) {
				costBaseDTO.setEndTime(it.getKey());
			} else {
				costBaseDTO.setStartTime(costBaseDTO.getEndTime());
				costBaseDTO.setEndTime(it.getKey());
			}
			renterOrderCostDetailEntityList.add(getRentAmtEntity(costBaseDTO, it.getValue()));
			i++;
		}
		return renterOrderCostDetailEntityList;
	}
	
	private RenterOrderCostDetailEntity getRentAmtEntity(CostBaseDTO costBaseDTO, List<RenterGoodsPriceDetailDTO> dayPrices) {
		// TODO 走配置中心获取
		Integer configHours = 8;
		// 数据转化
		List<CarPriceOfDay> carPriceOfDayList = dayPrices.stream().map(dayPrice -> {
			CarPriceOfDay carPriceOfDay = new CarPriceOfDay();
			carPriceOfDay.setCurDate(dayPrice.getCarDay());
			carPriceOfDay.setDayPrice(dayPrice.getCarUnitPrice());
			return carPriceOfDay;
		}).collect(Collectors.toList());
		// 计算租金
		FeeResult feeResult = RenterFeeCalculatorUtils.calRentAmt(costBaseDTO.getStartTime(), costBaseDTO.getEndTime(), configHours, carPriceOfDayList);
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.RENT_AMT);
		return result;
	}
	
	
	
	/**
	 * 获取平台手续费返回结果
	 * @param costBaseDTO
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity getServiceChargeFeeEntity(CostBaseDTO costBaseDTO) {
		log.info("getServiceChargeFeeEntity costBaseDTO=[{}]",costBaseDTO);
		if (costBaseDTO == null) {
			log.error("getServiceChargeFeeEntity 获取平台手续费costBaseDTO对象为空");
			Cat.logError("获取平台手续费costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		FeeResult feeResult = RenterFeeCalculatorUtils.calServiceChargeFee();
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.FEE);
		return result;
	}
	
	
	/**
	 * 获取平台保障费返回结果
	 * @param insurAmtDTO
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity getInsurAmtEntity(InsurAmtDTO insurAmtDTO) {
		log.info("getInsurAmtEntity insurAmtDTO=[{}]",insurAmtDTO);
		if (insurAmtDTO == null) {
			log.error("getInsurAmtEntity 获取平台保障费insurAmtDTO对象为空");
			Cat.logError("获取平台保障费insurAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = insurAmtDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getInsurAmtEntity 获取平台保障费insurAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取平台保障费insurAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 走配置中心获取
		Integer configHours = 8;
		// TODO 走配置中心获取
		List<InsuranceConfig> insuranceConfigs = null;
		// 指导价
		Integer guidPrice = insurAmtDTO.getGuidPrice();
		if (insurAmtDTO.getInmsrp() != null && insurAmtDTO.getInmsrp() != 0) {
			guidPrice = insurAmtDTO.getInmsrp();
		}
		// 会员系数
		Double coefficient = CommonUtils.getDriveAgeCoefficientByDri(insurAmtDTO.getCertificationTime());
		// 车辆标签系数
		Double easyCoefficient = CommonUtils.getEasyCoefficient(insurAmtDTO.getCarLabelIds());
		FeeResult feeResult = RenterFeeCalculatorUtils.calInsurAmt(costBaseDTO.getStartTime(), costBaseDTO.getEndTime(), 
				insurAmtDTO.getGetCarBeforeTime(), insurAmtDTO.getReturnCarAfterTime(), configHours, guidPrice, coefficient, easyCoefficient, insuranceConfigs);
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.INSURE_TOTAL_PRICES);
		return result;
	}
	
	
	/**
	 * 获取全面保障费返回结果
	 * @param abatementAmtDTO
	 * @return List<RenterOrderCostDetailEntity>
	 */
	public List<RenterOrderCostDetailEntity> listAbatementAmtEntity(AbatementAmtDTO abatementAmtDTO) {
		log.info("listAbatementAmtEntity abatementAmtDTO=[{}]",abatementAmtDTO);
		if (abatementAmtDTO == null) {
			log.error("listAbatementAmtEntity 获取全面保障费abatementAmtDTO对象为空");
			Cat.logError("获取全面保障费abatementAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = abatementAmtDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("listAbatementAmtEntity 获取全面保障费abatementAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取全面保障费abatementAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 走配置中心获取
		Integer configHours = 8;
		// 指导价
		Integer guidPrice = abatementAmtDTO.getGuidPrice();
		if (abatementAmtDTO.getInmsrp() != null && abatementAmtDTO.getInmsrp() != 0) {
			guidPrice = abatementAmtDTO.getInmsrp();
		}
		// 会员系数
		Double coefficient = CommonUtils.getDriveAgeCoefficientByDri(abatementAmtDTO.getCertificationTime());
		// 车辆标签系数
		Double easyCoefficient = CommonUtils.getEasyCoefficient(abatementAmtDTO.getCarLabelIds());
		List<FeeResult> feeResultList = RenterFeeCalculatorUtils.calcAbatementAmt(costBaseDTO.getStartTime(), costBaseDTO.getEndTime(), abatementAmtDTO.getGetCarBeforeTime(), abatementAmtDTO.getReturnCarAfterTime(), configHours, guidPrice, coefficient, easyCoefficient);
		List<RenterOrderCostDetailEntity> resultList = feeResultList.stream().map(fr -> costBaseConvert(costBaseDTO, fr, RenterCashCodeEnum.ABATEMENT_INSURE)).collect(Collectors.toList());
		return resultList;
	}
	
	/**
	 * 获取附加驾驶人费用返回结果
	 * @param extraDriverDTO
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity getExtraDriverInsureAmtEntity(ExtraDriverDTO extraDriverDTO) {
		log.info("getExtraDriverInsureAmtEntity extraDriverDTO=[{}]",extraDriverDTO);
		if (extraDriverDTO == null) {
			log.error("getExtraDriverInsureAmtEntity 获取附加驾驶人费用extraDriverDTO对象为空");
			Cat.logError("获取附加驾驶人费用extraDriverDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = extraDriverDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getExtraDriverInsureAmtEntity 获取附加驾驶人费用extraDriverDTO.costBaseDTO对象为空");
			Cat.logError("获取附加驾驶人费用extraDriverDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 走配置中心取单价
		Integer unitExtraDriverInsure = 20;
		List<String> driverIds = extraDriverDTO.getDriverIds();
		Integer extraDriverCount = (driverIds == null || driverIds.isEmpty()) ? 0:driverIds.size();
		FeeResult feeResult = RenterFeeCalculatorUtils.calExtraDriverInsureAmt(unitExtraDriverInsure, extraDriverCount, costBaseDTO.getStartTime(), costBaseDTO.getEndTime());
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.EXTRA_DRIVER_INSURE);
		return result;
	}
	
	/**
	 * 获取超里程费用
	 * @param mileageAmtDTO
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity getMileageAmtEntity(MileageAmtDTO mileageAmtDTO) {
		log.info("getMileageAmtEntity mileageAmtDTO=[{}]",mileageAmtDTO);
		if (mileageAmtDTO == null) {
			log.error("getMileageAmtEntity 获取超里程费用mileageAmtDTO对象为空");
			Cat.logError("获取超里程费用mileageAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = mileageAmtDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getMileageAmtEntity 获取超里程费用mileageAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取超里程费用mileageAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 走配置中心获取
		Integer configHours = 8;
		Integer mileageAmt = RenterFeeCalculatorUtils.calMileageAmt(mileageAmtDTO.getDayMileage(), mileageAmtDTO.getGuideDayPrice(), 
				mileageAmtDTO.getGetmileage(), mileageAmtDTO.getReturnMileage(), costBaseDTO.getStartTime(), costBaseDTO.getEndTime(), configHours);
		FeeResult feeResult = new FeeResult();
		feeResult.setTotalFee(mileageAmt);
		feeResult.setUnitCount(1.0);
		feeResult.setUnitPrice(mileageAmt);
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.MILEAGE_COST_RENTER);
		return result;
	}
	
	
	/**
	 * 获取租客油费
	 * @param oilAmtDTO
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity getOilAmtEntity(OilAmtDTO oilAmtDTO) {
		log.info("getOilAmtEntity oilAmtDTO=[{}]",oilAmtDTO);
		if (oilAmtDTO == null) {
			log.error("getOilAmtEntity 获取租客油费oilAmtDTO对象为空");
			Cat.logError("获取租客油费oilAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = oilAmtDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getOilAmtEntity 获取租客油费oilAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取租客油费oilAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 走配置中心获取
		List<OilAverageCostBO> oilAverageList = null;
		Integer oilAmt = RenterFeeCalculatorUtils.calOilAmt(oilAmtDTO.getCityCode(), oilAmtDTO.getOilVolume(), oilAmtDTO.getEngineType(), 
				oilAmtDTO.getGetOilScale(), oilAmtDTO.getReturnOilScale(), oilAverageList, oilAmtDTO.getOilScaleDenominator());
		FeeResult feeResult = new FeeResult();
		feeResult.setTotalFee(oilAmt);
		feeResult.setUnitCount(1.0);
		feeResult.setUnitPrice(oilAmt);
		RenterOrderCostDetailEntity result = costBaseConvert(costBaseDTO, feeResult, RenterCashCodeEnum.OIL_COST_RENTER);
		return result;
	}
	
	
	/**
	 * 获取车辆押金对象
	 * @param depositAmtDTO
	 * @return CarDepositAmtVO
	 */
	public CarDepositAmtVO getCarDepositAmtVO(DepositAmtDTO depositAmtDTO) {
		log.info("getCarDepositAmtVO depositAmtDTO=[{}]",depositAmtDTO);
		if (depositAmtDTO == null) {
			log.error("getCarDepositAmtVO 获取车辆押金对象depositAmtDTO对象为空");
			Cat.logError("获取车辆押金对象depositAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 押金配置列表从配置中心获取
		List<DepositText> depositList = null;
		CarDepositAmtVO carDepositAmtVO = RenterFeeCalculatorUtils.calCarDepositAmt(depositAmtDTO.getInternalStaff(), depositAmtDTO.getCityCode(), 
				depositAmtDTO.getSurplusPrice(), depositAmtDTO.getCarBrandTypeRadio(), depositAmtDTO.getCarYearRadio(), 
				depositList, depositAmtDTO.getReliefPercetage());
		return carDepositAmtVO;
	}
	
	
	/**
	 * 获取违章押金
	 * @param illegalDepositAmtDTO
	 * @return Integer
	 */
	public Integer getIllegalDepositAmt(IllegalDepositAmtDTO illDTO) {
		log.info("getIllegalDepositAmt illegalDepositAmtDTO=[{}]",illDTO);
		if (illDTO == null) {
			log.error("getIllegalDepositAmt 获取违章押金illegalDepositAmtDTO对象为空");
			Cat.logError("获取违章押金illegalDepositAmtDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		CostBaseDTO costBaseDTO = illDTO.getCostBaseDTO();
		if (costBaseDTO == null) {
			log.error("getIllegalDepositAmt 获取违章押金illegalDepositAmtDTO.costBaseDTO对象为空");
			Cat.logError("获取违章押金illegalDepositAmtDTO.costBaseDTO对象为空", new RenterCostParameterException());
			throw new RenterCostParameterException();
		}
		// TODO 特殊城市（逗号分隔的城市编码）从配置中心获取
		String specialCityCodes = null;
		// TODO 特殊车牌合特殊城市对应的特殊押金值从配置中心获取
		Integer specialIllegalDepositAmt = null;
		// TODO 违章押金配置从配置中心获取
		List<IllegalDepositConfig> illegalDepositList = null;
		Integer illegalDepositAmt = RenterFeeCalculatorUtils.calIllegalDepositAmt(illDTO.getInternalStaff(), illDTO.getCityCode(), illDTO.getCarPlateNum(), 
				specialCityCodes, specialIllegalDepositAmt, illegalDepositList, 
				costBaseDTO.getStartTime(), costBaseDTO.getEndTime());
		return illegalDepositAmt;
	}
	
	
	
	
	/**
	 * 获取租客应付(正常订单流转)
	 * @param orderNo 主订单号
	 * @param renterOrderNo 租客订单号
	 * @return Integer
	 */
	public Integer getPayable(String orderNo, String renterOrderNo, String memNo) {
		// 获取费用明细
		List<RenterOrderCostDetailEntity> costList = renterOrderCostDetailService.listRenterOrderCostDetail(orderNo, renterOrderNo);
		// 获取补贴明细
		List<RenterOrderSubsidyDetailEntity> subsidyList = renterOrderSubsidyDetailService.listRenterOrderSubsidyDetail(orderNo, renterOrderNo);
		// 罚金
		List<RenterOrderFineDeatailEntity> fineList = renterOrderFineDeatailService.listRenterOrderFineDeatail(orderNo, renterOrderNo);
		// 管理后台补贴
		List<OrderConsoleCostDetailEntity> consoleCostList = orderConsoleCostDetailService.listOrderConsoleCostDetail(orderNo,memNo);
		Integer payable = 0;
		if (costList != null && !costList.isEmpty()) {
			payable += costList.stream().mapToInt(RenterOrderCostDetailEntity::getTotalAmount).sum();
		}
		if (subsidyList != null && !subsidyList.isEmpty()) {
			payable += subsidyList.stream().mapToInt(RenterOrderSubsidyDetailEntity::getSubsidyAmount).sum();
		}
		if (fineList != null && !fineList.isEmpty()) {
			payable += fineList.stream().mapToInt(RenterOrderFineDeatailEntity::getFineAmount).sum();
		}
		if (consoleCostList != null && !consoleCostList.isEmpty()) {
			payable += consoleCostList.stream().mapToInt(OrderConsoleCostDetailEntity::getSubsidyAmount).sum();
		}
		return payable;
	}
	
	
	/**
	 * 数据转化
	 * @param costBaseDTO 基本参数
	 * @param feeResult 计算结果对象
	 * @return RenterOrderCostDetailEntity
	 */
	public RenterOrderCostDetailEntity costBaseConvert(CostBaseDTO costBaseDTO, FeeResult feeResult, RenterCashCodeEnum renterCashCodeEnum) {
		if (costBaseDTO == null) {
			return null;
		}
		if (feeResult == null) {
			return null;
		}
		if (renterCashCodeEnum == null) {
			return null;
		}
		Integer totalFee = feeResult.getTotalFee() == null ? 0:feeResult.getTotalFee();
		if (RENTERCASHCODEENUM_LIST.contains(renterCashCodeEnum)) {
			totalFee = -totalFee;
		}
		RenterOrderCostDetailEntity result = new RenterOrderCostDetailEntity();
		result.setOrderNo(costBaseDTO.getOrderNo());
		result.setRenterOrderNo(costBaseDTO.getRenterOrderNo());
		result.setMemNo(costBaseDTO.getMemNo());
		result.setStartTime(costBaseDTO.getStartTime());
		result.setEndTime(costBaseDTO.getEndTime());
		result.setUnitPrice(feeResult.getUnitPrice());
		result.setCount(feeResult.getUnitCount());
		result.setTotalAmount(totalFee);
		result.setCostCode(renterCashCodeEnum.getCashNo());
		result.setCostDesc(renterCashCodeEnum.getTxt());
		return result;
	}

	
    /*
     * @Author ZhangBin
     * @Date 2019/12/26 10:50
     * @Description: 计算取还车费用
     *
     **/
    public GetReturnCostDto getReturnCarCost(GetReturnCarCostReqDto getReturnCarCostReqDto) {
        GetReturnCostDto getReturnCostDto = new GetReturnCostDto();
        List<RenterOrderCostDetailEntity> listCostDetail = new ArrayList<>();
        List<RenterOrderSubsidyDetailEntity> listCostSubsidy = new ArrayList<>();
        GetReturnResponseVO getReturnResponse = new GetReturnResponseVO();

        CostBaseDTO costBaseDTO = getReturnCarCostReqDto.getCostBaseDTO();
        // 城市编码
        Integer cityCode = getReturnCarCostReqDto.getCityCode();
        // 订单来源
        Integer source = getReturnCarCostReqDto.getSource();
        // 场景号
        String entryCode = getReturnCarCostReqDto.getEntryCode();
        // 取车经度
        String srvGetLon = getReturnCarCostReqDto.getSrvGetLon();
        // 取车纬度
        String srvGetLat = getReturnCarCostReqDto.getSrvGetLat();
        // 还车经度
        String srvReturnLon = getReturnCarCostReqDto.getSrvReturnLon();
        // 还车纬度
        String srvReturnLat = getReturnCarCostReqDto.getSrvReturnLat();
        // 车辆经度
        String carLon = getReturnCarCostReqDto.getCarLon();
        // 车辆纬度
        String carLat = getReturnCarCostReqDto.getCarLat();
        boolean getFlag = StringUtils.isBlank(srvGetLon) || StringUtils.isBlank(srvGetLat) || "0.0".equalsIgnoreCase(srvGetLon) || "0.0".equalsIgnoreCase(srvGetLat);
        boolean returnFlag = StringUtils.isBlank(srvReturnLon) || StringUtils.isBlank(srvReturnLat) || "0.0".equalsIgnoreCase(srvReturnLon) || "0.0".equalsIgnoreCase(srvReturnLat);
        City city = null;
        if (getFlag || returnFlag) {
            //TODO 配置中获取
            //city = cityMapper.getCityLonAndLatByCode(cityCode);
        }
        if (getFlag && city != null) {
            srvGetLon = city.getLon();
            srvGetLat = city.getLat();
        }
        if (returnFlag && city != null) {
            srvReturnLon = city.getLon();
            srvReturnLat = city.getLat();
        }
        //获取取车的距离
        Float getDistance = this.getRealDistance(srvGetLon, srvGetLat, carLon, carLat);
        //获取还车的距离
        Float returnDistance = this.getRealDistance(srvReturnLon, srvReturnLat, carLon, carLat);
        String channelCode = getChannelCodeByEntryCode(entryCode).getTypeCode();
        if(getReturnCarCostReqDto.getIsPackageOrder() != null && getReturnCarCostReqDto.getIsPackageOrder()){
            channelCode = getChannelCode(source).getTypeCode();
        }
        // 租金+保险+不计免赔+手续费
        Integer sumJudgeFreeFee = getReturnCarCostReqDto.getSumJudgeFreeFee();
        // 订单的租金，平台保障费，全面保障费，平台手续费总和小于300，则取还车服务不享受免费
        sumJudgeFreeFee = sumJudgeFreeFee == null ? 0:sumJudgeFreeFee;
        String sumJudgeFreeFeeStr = String.valueOf(sumJudgeFreeFee);
        log.info("取还车费用计算，租金+保险+不计免赔+手续费 sumJudgeFreeFee=[{}]", sumJudgeFreeFee);

        List<GetFbcFeeRequestDetail> reqVOList = new ArrayList<>();
        GetFbcFeeRequestDetail getReqVO = new GetFbcFeeRequestDetail();
        getReqVO.setChannelName(channelCode);
        getReqVO.setRequestTime(LocalDateTimeUtils.getNowDateLong());
        getReqVO.setGetReturnType("get");
        getReqVO.setGetReturnTime(String.valueOf(LocalDateTimeUtils.localDateTimeToLong(costBaseDTO.getStartTime())));
        getReqVO.setCityId(String.valueOf(cityCode));
        getReqVO.setOrderType(this.getIsPackageOrder(getReturnCarCostReqDto.getIsPackageOrder()));
        getReqVO.setDistance(String.valueOf(getDistance));
        if(getFlag && city!=null) {
            getReqVO.setRenterLocation(city.getLon()+","+city.getLat());
        } else {
            getReqVO.setRenterLocation(srvGetLon+","+srvGetLat);
        }
        getReqVO.setSumJudgeFreeFee(sumJudgeFreeFeeStr);
        reqVOList.add(getReqVO);

        GetFbcFeeRequestDetail returnReqVO = new GetFbcFeeRequestDetail();
        returnReqVO.setChannelName(channelCode);
        returnReqVO.setRequestTime(LocalDateTimeUtils.getNowDateLong());
        returnReqVO.setGetReturnType("return");
        returnReqVO.setGetReturnTime(String.valueOf(LocalDateTimeUtils.localDateTimeToLong(costBaseDTO.getEndTime())));
        returnReqVO.setCityId(String.valueOf(cityCode));
        returnReqVO.setOrderType(this.getIsPackageOrder(getReturnCarCostReqDto.getIsPackageOrder()));
        returnReqVO.setDistance(String.valueOf(returnDistance));
        if(returnFlag && city!=null) {
            returnReqVO.setRenterLocation(city.getLon()+","+city.getLat());
        } else {
            returnReqVO.setRenterLocation(srvReturnLon+","+srvReturnLat);
        }
        returnReqVO.setSumJudgeFreeFee(sumJudgeFreeFeeStr);
        reqVOList.add(returnReqVO);

        GetFbcFeeRequest getFbcFeeRequest = new GetFbcFeeRequest();
        getFbcFeeRequest.setReq(reqVOList);
        com.dianping.cat.message.Transaction t = null;
        try {
            log.info("始请求取还车费用计算服务，请求参数为：{}", GsonUtils.toJson(reqVOList));
            t = Cat.newTransaction(CatConstants.TYPE_URL, "");
            //TODO apollo获取参数
            ResponseEntity<HttpResult> responseEntity = restTemplate.postForEntity(/*apolloCostConfig.getGetfbcfeeUrl() + */"/upPricefetchbackCarFee/getFbcFee", reqVOList, HttpResult.class);

            if (null != responseEntity) {
                HttpResult httpResult = responseEntity.getBody();
                if (!httpResult.getResCode().equals(ErrorCode.SUCCESS.getCode())) {
                    log.error("警告:GetReturnCarFeeV55Service---请求取还车费用计算服务失败,错误码:{},错误信息:{}", httpResult.getData(), httpResult.getResMsg());
                    Cat.logError("警告:GetReturnCarFeeV55Service---请求取还车费用计算服务失败", new Exception(httpResult.getResMsg()));
                    t.setStatus("-2");
                    Cat.logEvent("getGetReturnFee", "调用取还车费用计算服务失败，错误码 :" + httpResult.getData() + ",错误信息:" + httpResult.getResMsg());
                    return null;
                }
                List<PriceFbcFeeResponseDetail> fbcFeeResults = this.convertPriceResponseJson(JSON.toJSONString(httpResult.getData()));
                if (CollectionUtils.isEmpty(fbcFeeResults)) {
                    log.info("GetReturnCarFeeV55Service---请求取还车费用计算服务返回结果为空：getFbcFeeResponse={null}");
                    t.setStatus("-3");
                    Cat.logEvent("getGetReturnFee", "调用取还车费用计算失败，返回空");
                    return null;
                }
                log.info("GetReturnCarFeeV55Service---请求取还车费用计算服务返回参数：getFbcFeeResponse={}", fbcFeeResults);
                getReturnResponse.setSumJudgeFreeFee(sumJudgeFreeFee);
                fbcFeeResults.forEach(fbcFeeResponse -> {
                    if ("get".equalsIgnoreCase(fbcFeeResponse.getGetReturnType())) {
                        RenterOrderCostDetailEntity renterOrderCostDetailEntity = new RenterOrderCostDetailEntity();
                        renterOrderCostDetailEntity.setCostCode(RenterCashCodeEnum.SRV_GET_COST.getCashNo());
                        renterOrderCostDetailEntity.setCostDesc(RenterCashCodeEnum.SRV_GET_COST.getTxt());
                        renterOrderCostDetailEntity.setCount(1D);
                        renterOrderCostDetailEntity.setTotalAmount(Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        listCostDetail.add(renterOrderCostDetailEntity);

                        RenterOrderSubsidyDetailEntity renterOrderSubsidyDetailEntity = new RenterOrderSubsidyDetailEntity();
                        renterOrderSubsidyDetailEntity.setSubsidType(SubsidyTypeCodeEnum.GET_CAR.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidyTypeCode(SubsidyTypeCodeEnum.GET_CAR.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidySourceCode(SubsidySourceCodeEnum.PLATFORM.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidySource(SubsidySourceCodeEnum.PLATFORM.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidyCode(SubsidySourceCodeEnum.RENTER.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidyDesc("平台补贴给租客的取车费用！");
                        renterOrderSubsidyDetailEntity.setSubsidyAmount(Integer.valueOf(fbcFeeResponse.getExpectedRealFee()) - Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        renterOrderSubsidyDetailEntity.setSubsidyVoucher("");
                        listCostSubsidy.add(renterOrderSubsidyDetailEntity);

                        getReturnResponse.setGetFee(Integer.valueOf(fbcFeeResponse.getExpectedRealFee()));
                        getReturnResponse.setGetShouldFee(Integer.valueOf(fbcFeeResponse.getExpectedShouldFee()));
                        getReturnResponse.setGetInitFee(Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        getReturnResponse.setGetTimePeriodUpPrice(fbcFeeResponse.getTimePeriodUpPrice());
                        getReturnResponse.setGetDistanceUpPrice(fbcFeeResponse.getDistanceUpPrice());
                        getReturnResponse.setGetCicrleUpPrice(fbcFeeResponse.getCicrleUpPrice());
                        getReturnResponse.setGetShowDistance(Double.valueOf(fbcFeeResponse.getShowDistance()));


                    } else if ("return".equalsIgnoreCase(fbcFeeResponse.getGetReturnType())) {
                        RenterOrderCostDetailEntity renterOrderCostDetailEntity = new RenterOrderCostDetailEntity();
                        renterOrderCostDetailEntity.setCostCode(RenterCashCodeEnum.SRV_RETURN_COST.getCashNo());
                        renterOrderCostDetailEntity.setCostDesc(RenterCashCodeEnum.SRV_RETURN_COST.getTxt());
                        renterOrderCostDetailEntity.setCount(1D);
                        renterOrderCostDetailEntity.setTotalAmount(Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        listCostDetail.add(renterOrderCostDetailEntity);

                        RenterOrderSubsidyDetailEntity renterOrderSubsidyDetailEntity = new RenterOrderSubsidyDetailEntity();
                        renterOrderSubsidyDetailEntity.setSubsidType(SubsidyTypeCodeEnum.RETURN_CAR.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidyTypeCode(SubsidyTypeCodeEnum.RETURN_CAR.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidySourceCode(SubsidySourceCodeEnum.PLATFORM.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidySource(SubsidySourceCodeEnum.PLATFORM.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidyCode(SubsidySourceCodeEnum.RENTER.getCode());
                        renterOrderSubsidyDetailEntity.setSubsidyName(SubsidySourceCodeEnum.RENTER.getDesc());
                        renterOrderSubsidyDetailEntity.setSubsidyDesc("平台补贴给租客的还车费用！");
                        renterOrderSubsidyDetailEntity.setSubsidyAmount(Integer.valueOf(fbcFeeResponse.getExpectedRealFee()) - Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        renterOrderSubsidyDetailEntity.setSubsidyVoucher("");
                        listCostSubsidy.add(renterOrderSubsidyDetailEntity);

                        getReturnResponse.setReturnFee(Integer.valueOf(fbcFeeResponse.getExpectedRealFee()));
                        getReturnResponse.setReturnShouldFee(Integer.valueOf(fbcFeeResponse.getExpectedShouldFee()));
                        getReturnResponse.setReturnInitFee(Integer.valueOf(fbcFeeResponse.getBaseFee()));
                        getReturnResponse.setReturnTimePeriodUpPrice(fbcFeeResponse.getTimePeriodUpPrice());
                        getReturnResponse.setReturnDistanceUpPrice(fbcFeeResponse.getDistanceUpPrice());
                        getReturnResponse.setReturnCicrleUpPrice(fbcFeeResponse.getCicrleUpPrice());
                        getReturnResponse.setReturnShowDistance(Double.valueOf(fbcFeeResponse.getShowDistance()));
                    }
                });
                t.setStatus(Transaction.SUCCESS);
                Cat.logEvent("getGetReturnFee", "调用取还车费用计算服务成功");
            }
        } catch (Exception e) {
            if (t != null) {
                t.setStatus("-4");
                Cat.logEvent("getGetReturnFee", "调用取还车费用计算服务异常：" + e.getMessage());
                Cat.logError("警告:GetReturnCarFeeV55Service---请求取还车费用计算服务异常", e);
            }
            log.error("警告:GetReturnCarFeeV55Service---请求取还车费用计算服务异常,{}", e);
        } finally {
            if (t != null) {
                t.complete();
            }
        }
        getReturnCostDto.setGetReturnResponseVO(getReturnResponse);
        getReturnCostDto.setRenterOrderCostDetailEntityList(listCostDetail);
        getReturnCostDto.setRenterOrderSubsidyDetailEntityList(listCostSubsidy);
        return getReturnCostDto;
    }

    private List<PriceFbcFeeResponseDetail> convertPriceResponseJson(String result) {
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(result);
        if (!StringUtils.isBlank(jsonObject.getString("fbcFeeResults"))) {
            List<PriceFbcFeeResponseDetail> fbcFeeResults = JSONArray.parseArray(jsonObject.getString("fbcFeeResults"), PriceFbcFeeResponseDetail.class);
            return fbcFeeResults;
        }
        return null;
    }


    /**
     *
     * 取还车订单类型
     * @param isPackageOrder
     * @return
     */
    private String getIsPackageOrder(Boolean isPackageOrder){
        if(isPackageOrder != null && isPackageOrder){
            // 套餐订单
            return "package";
        }else{
            // 普通订单
            return "general";
        }
    }
    /**
     * 根据来源判断渠道
     * @param source
     * @return
     */
    private ChannelNameTypeEnum getChannelCode(Integer source){
        if(null == source){
            return ChannelNameTypeEnum.APP;
        }

        //携程：400，同程：401，平安
        if(source.intValue() == 400 || source.intValue() == 401 || source.intValue() == 402){
            return ChannelNameTypeEnum.OTA;
        }

        return ChannelNameTypeEnum.APP;
    }

    /**
     * 计算取还车距离
     * @return
     */
    private static Float getRealDistance(String carLon,String carLat,String origionCarLon,String originCarLat){
        try {
            if(StringUtils.isBlank(carLon) || StringUtils.isBlank(carLat)
                    || StringUtils.isBlank(origionCarLon) || StringUtils.isBlank(originCarLat)) {

                return 0F;
            }
            return (float) calcDistance(Double.valueOf(carLon),Double.valueOf(carLat),
                    Double.valueOf(origionCarLon), Double.valueOf(originCarLat));
        } catch (Exception e) {
            log.error("getRealDistance计算取还车距离报错距离返回0：",e);
        }
        return 0F;
    }

    /**
     * 计算距离 (和数据库算法统一)
     * @param originCarLat
     * @param origionCarLon
     * @param carLat
     * @param carLon
     * @return
     */
    private static double calcDistance(double carLon,double carLat,double origionCarLon,double originCarLat){
        return new BigDecimal(
                6378.137*2*Math.asin(Math.sqrt(Math.pow(Math.sin( (originCarLat*Math.PI/180-carLat*Math.PI/180)/2),2)
                        +Math.cos(originCarLat*Math.PI/180)*Math.cos(carLat*Math.PI/180)*
                        Math.pow(Math.sin( (origionCarLon*Math.PI/180-carLon*Math.PI/180)/2),2))))
                .doubleValue();
    }


    /**
     * 根据entryCode判断渠道
     * @param entryCode
     * @return
     */
    private ChannelNameTypeEnum getChannelCodeByEntryCode(String entryCode){
        if(StringUtils.isEmpty(entryCode)){
            return ChannelNameTypeEnum.APP;
        }

        //ota平台  EX021只代表订单为套餐
        if(entryCode.equals("ota")){
            return ChannelNameTypeEnum.OTA;
        }
        //代步车渠道、安联
        else if(entryCode.equals("EX011") || entryCode.equals("EX022") ||
                entryCode.equals("EX030") || entryCode.equals("scooter")){
            return ChannelNameTypeEnum.SCOOTER;
        }
        //App
        else if(entryCode.equals("app")){
            return ChannelNameTypeEnum.APP;
        }

        return ChannelNameTypeEnum.APP;
    }
}
