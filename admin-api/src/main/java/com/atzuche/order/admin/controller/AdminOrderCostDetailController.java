/**
 * 
 */
package com.atzuche.order.admin.controller;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.admin.constant.AdminOpTypeEnum;
import com.atzuche.order.admin.service.OrderCostDetailService;
import com.atzuche.order.admin.service.OrderCostRemoteService;
import com.atzuche.order.admin.service.OwnerOrderDetailService;
import com.atzuche.order.admin.service.RemoteFeignService;
import com.atzuche.order.admin.service.log.AdminLogService;
import com.atzuche.order.admin.util.CompareBeanUtils;
import com.atzuche.order.admin.vo.req.cost.RenterCostReqVO;
import com.atzuche.order.admin.vo.resp.cost.AdditionalDriverInsuranceVO;
import com.atzuche.order.admin.vo.resp.income.RenterToPlatformVO;
import com.atzuche.order.admin.vo.resp.order.cost.detail.OrderRenterFineAmtDetailResVO;
import com.atzuche.order.admin.vo.resp.order.cost.detail.PlatformToRenterSubsidyResVO;
import com.atzuche.order.admin.vo.resp.order.cost.detail.ReductionDetailResVO;
import com.atzuche.order.admin.vo.resp.order.cost.detail.RenterPriceAdjustmentResVO;
import com.atzuche.order.commons.BindingResultUtil;
import com.atzuche.order.commons.CostStatUtils;
import com.atzuche.order.commons.entity.orderDetailDto.OwnerOrderSubsidyDetailDTO;
import com.atzuche.order.commons.entity.orderDetailDto.RenterAdditionalDriverDTO;
import com.atzuche.order.commons.entity.orderDetailDto.OrderDetailReqDTO;
import com.atzuche.order.commons.entity.orderDetailDto.OrderStatusRespDTO;
import com.atzuche.order.commons.entity.ownerOrderDetail.PlatformToOwnerSubsidyDTO;
import com.atzuche.order.commons.entity.ownerOrderDetail.RenterRentDetailDTO;
import com.atzuche.order.commons.entity.rentCost.RenterCostDetailDTO;
import com.atzuche.order.commons.enums.OrderStatusEnum;
import com.atzuche.order.commons.enums.cashcode.FineTypeCashCodeEnum;
import com.atzuche.order.commons.enums.cashcode.RenterCashCodeEnum;
import com.atzuche.order.commons.exceptions.OrderIngNotOperateException;
import com.atzuche.order.commons.exceptions.OrderStatusNotFoundException;
import com.atzuche.order.commons.vo.rentercost.RenterAndConsoleFineVO;
import com.atzuche.order.commons.vo.req.AdditionalDriverInsuranceIdsReqVO;
import com.atzuche.order.commons.vo.req.RenterAdjustCostReqVO;
import com.atzuche.order.commons.vo.res.DangerCountRespVO;
import com.atzuche.order.commons.vo.res.rentcosts.ConsoleRenterOrderFineDeatailEntity;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.doc.annotation.AutoDocMethod;
import com.autoyol.doc.annotation.AutoDocVersion;
import com.dianping.cat.Cat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author jing.huang
 *
 */
@Slf4j
@AutoDocVersion(version = "管理后台租客租车费用 租客费用页面 相关明细接口文档")
@RestController
@RequestMapping("/console/ordercost/detail/")
public class AdminOrderCostDetailController {
	private static final Logger logger = LoggerFactory.getLogger(AdminOrderCostDetailController.class);
	
	@Autowired
	OrderCostDetailService orderCostDetailService;
    @Autowired
    AdminLogService adminLogService;
    @Autowired
    OrderCostRemoteService orderCostRemoteService;
    @Autowired
    RemoteFeignService remoteFeignService;
    @Autowired
    OwnerOrderDetailService ownerOrderDetailService;
	
	@AutoDocMethod(description = "违约罚金 修改违约罚金", value = "违约罚金 修改违约罚金",response = ResponseData.class)
    @PostMapping("fineAmt/update")
    public ResponseData<?> updatefineAmtListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.RenterFineCostReqVO renterCostReqVO, BindingResult bindingResult) {
    	logger.info("updatefineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        RenterAndConsoleFineVO fineList = null;
        try{
            fineList = orderCostRemoteService.getRenterAndConsoleFineVO(renterCostReqVO.getOrderNo(), renterCostReqVO.getRenterOrderNo());
        }catch (Exception e){
            log.error("违约罚金-记录日志查询异常",e);
        }
        try {
        	//无需返回值
        	orderCostDetailService.updatefineAmtListByOrderNo(renterCostReqVO);
            //记录日志
        	try{

                List<ConsoleRenterOrderFineDeatailEntity> list = fineList == null ? null : fineList.getConsoleFineList();
                // 累计求和
                int renterBeforeReturnCarFineAmount = 0;
                int renterDelayReturnCarFineAmount = 0;
                for (ConsoleRenterOrderFineDeatailEntity consoleRenterOrderFineDeatailEntity : list) {
                    if (consoleRenterOrderFineDeatailEntity.getFineType().intValue() == FineTypeCashCodeEnum.MODIFY_ADVANCE.getFineType().intValue()) {
                        renterBeforeReturnCarFineAmount = consoleRenterOrderFineDeatailEntity.getFineAmount().intValue();
                    }else if (consoleRenterOrderFineDeatailEntity.getFineType().intValue() == FineTypeCashCodeEnum.DELAY_FINE.getFineType().intValue()) {
                        renterDelayReturnCarFineAmount = consoleRenterOrderFineDeatailEntity.getFineAmount().intValue();
                    }
                }
                String desc = FineTypeCashCodeEnum.MODIFY_ADVANCE.getFineTypeDesc()+": 原值: "+renterBeforeReturnCarFineAmount+" 修改为: " + -Integer.valueOf(renterCostReqVO.getRenterBeforeReturnCarFineAmt())+"\n"
                                + FineTypeCashCodeEnum.DELAY_FINE.getFineTypeDesc() + ": 原值: "+ renterDelayReturnCarFineAmount+ " 修改为: "+ -Integer.valueOf(renterCostReqVO.getRenterDelayReturnCarFineAmt());
                adminLogService.insertLog(AdminOpTypeEnum.RENTER_UPDATE_FINE, renterCostReqVO.getOrderNo(), desc);
            }catch (Exception e){
        	    log.error("记录日志失败",e);
            }
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updatefineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("updatefineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
	
    /**
     * 违约罚金
     * @param ownerInComeReqVO
     * @return
     */
    @AutoDocMethod(description = "违约罚金 违约罚金明细", value = "违约罚金 违约罚金明细",response = OrderRenterFineAmtDetailResVO.class)
    @PostMapping("fineAmt/list")
    public ResponseData<OrderRenterFineAmtDetailResVO> findfineAmtListByOrderNo(@RequestBody @Validated RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    
    /**
     *减免明细
     * @param rentalCostReqVO
     * @return
     */
    @AutoDocMethod(description = "减免明细 押金减免明细", value = "减免明细 押金减免明细",response = ReductionDetailResVO.class)
    @PostMapping("reductionDetails/list")
    public ResponseData<ReductionDetailResVO> findReductionDetailsListByOrderNo(@RequestBody @Validated RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findReductionDetailsListByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	ReductionDetailResVO resp = orderCostDetailService.findReductionDetailsListByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findReductionDetailsListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findReductionDetailsListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     *附加驾驶员险
     * @param rentalCostReqVO
     * @return
     */
    @AutoDocMethod(description = "附加驾驶员险 附加驾驶人明细", value = "附加驾驶员险 附加驾驶人明细",response = AdditionalDriverInsuranceVO.class)
    @PostMapping("additionalDriverInsurance/list")
    public ResponseData<?> findAdditionalDriverInsuranceByOrderNo(@RequestBody @Validated RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findAdditionalDriverInsuranceByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	AdditionalDriverInsuranceVO resp = orderCostDetailService.findAdditionalDriverInsuranceByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findAdditionalDriverInsuranceByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findAdditionalDriverInsuranceByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }

    /**
     *附加驾驶员险
     * @param additionalDriverInsuranceVO
     * @return
     */
    @AutoDocMethod(description = "新增附加驾驶员险  新增附加驾驶人", value = "新增附加驾驶员险  新增附加驾驶人",response = ResponseData.class)
    @PostMapping("additionalDriverInsurance/add")
    public ResponseData<?> insertAdditionalDriverInsuranceByOrderNo(@RequestBody @Validated AdditionalDriverInsuranceIdsReqVO renterCostReqVO,BindingResult bindingResult) {
    	logger.info("insertAdditionalDriverInsuranceByOrderNo controller params={}",renterCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        OrderDetailReqDTO orderDetailReqDTO = new OrderDetailReqDTO();
        orderDetailReqDTO.setOrderNo(renterCostReqVO.getOrderNo());
        orderDetailReqDTO.setRenterOrderNo(renterCostReqVO.getRenterOrderNo());
        ResponseData<OrderStatusRespDTO> respDTOResponseData = remoteFeignService.getOrderStatusFromRemote(orderDetailReqDTO);
        if(respDTOResponseData == null || respDTOResponseData.getData() == null || respDTOResponseData.getData().getOrderStatusDTO()==null){
            log.error("订单状态查询为空 orderDetailReqDTO={}",JSON.toJSONString(orderDetailReqDTO));
            throw new OrderStatusNotFoundException();
        }
        Integer status = respDTOResponseData.getData().getOrderStatusDTO().getStatus();
        if(status >= OrderStatusEnum.TO_RETURN_CAR.getStatus() || status == OrderStatusEnum.CLOSED.getStatus()){
            log.error("订单已经开始，不允许操作renterCostReqVO={}",JSON.toJSONString(renterCostReqVO));
            throw new OrderIngNotOperateException(renterCostReqVO.getOrderNo());
        }
        List<RenterAdditionalDriverDTO> oldData = null;
        try{
            oldData = remoteFeignService.queryAdditionalDriverListFromRemot(renterCostReqVO.getRenterOrderNo());
        }catch (Exception e){
            log.error("查询附加驾驶人异常",e);
        }
        try {
        	orderCostDetailService.insertAdditionalDriverInsuranceByOrderNo(renterCostReqVO);
            //日志记录
            try{
                if(oldData != null){
                    String desc = "附加驾驶人 由原来的 【";
                    List<RenterAdditionalDriverDTO> newData = remoteFeignService.queryAdditionalDriverListFromRemot(renterCostReqVO.getRenterOrderNo());
                    for (RenterAdditionalDriverDTO oldDatum : oldData) {
                        desc += oldDatum.getRealName()+" ";
                    }
                    desc += "】 修改为 【";
                    for (RenterAdditionalDriverDTO newDatum : newData) {
                        desc += newDatum.getRealName()+" ";
                    }
                    desc += "】";

                    adminLogService.insertLog(AdminOpTypeEnum.ADDITIONAL_DRIVER_ADD, renterCostReqVO.getOrderNo(), desc);
                }

            }catch (Exception e){
                log.error("记录日志失败",e);
            }
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("insertAdditionalDriverInsuranceByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("insertAdditionalDriverInsuranceByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     *平台给租客的补贴
     * @param rentalCostReqVO
     * @return
     */
    @AutoDocMethod(description = "平台给租客的补贴 平台给租客的补贴明细", value = "平台给租客的补贴 平台给租客的补贴明细",response = PlatformToRenterSubsidyResVO.class)
    @PostMapping("platFormToRenter/list")
    public ResponseData<PlatformToRenterSubsidyResVO> findPlatFormToRenterListByOrderNo(@RequestBody @Validated RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findPlatFormToRenterListByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	PlatformToRenterSubsidyResVO resp = orderCostDetailService.findPlatFormToRenterListByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findPlatFormToRenterListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findPlatFormToRenterListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     * 200119
     * @param renterCostReqVO
     * @param request
     * @param response
     * @param bindingResult
     * @return
     */
    @AutoDocMethod(description = "平台给租客的补贴 平台给租客的补贴修改", value = "平台给租客的补贴 平台给租客的补贴修改",response = ResponseData.class)
    @PostMapping("platFormToRenter/update")
    public ResponseData<?> updatePlatFormToRenterListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.PlatformToRenterSubsidyReqVO renterCostReqVO,BindingResult bindingResult) {
    	logger.info("updatePlatFormToRenterListByOrderNo controller params={}",renterCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        PlatformToRenterSubsidyResVO oldData = null;
        try{
            RenterCostReqVO req = new RenterCostReqVO();
            req.setOrderNo(renterCostReqVO.getOrderNo());
            req.setRenterOrderNo(renterCostReqVO.getRenterOrderNo());
            oldData = orderCostDetailService.findPlatFormToRenterListByOrderNo(req);
        }catch (Exception e){
            log.error("平台给租客的补贴-记录日志查询时异常",e);
        }
        try {
        	orderCostDetailService.updatePlatFormToRenterListByOrderNo(renterCostReqVO);
        	try{

                PlatformToRenterSubsidyResVO newData = new PlatformToRenterSubsidyResVO();
                newData.setDispatchingSubsidy(renterCostReqVO.getDispatchingSubsidy());
                newData.setOilSubsidy(renterCostReqVO.getOilSubsidy());
                newData.setCleanCarSubsidy(renterCostReqVO.getCleanCarSubsidy());
                newData.setGetReturnDelaySubsidy(renterCostReqVO.getGetReturnDelaySubsidy());
                newData.setDelaySubsidy(renterCostReqVO.getDelaySubsidy());
                newData.setTrafficSubsidy(renterCostReqVO.getTrafficSubsidy());
                newData.setInsureSubsidy(renterCostReqVO.getInsureSubsidy());
                newData.setRentAmtSubsidy(renterCostReqVO.getRentAmtSubsidy());
                newData.setOtherSubsidy(renterCostReqVO.getOtherSubsidy());
                newData.setAbatementSubsidy(renterCostReqVO.getAbatementSubsidy());
                newData.setFeeSubsidy(renterCostReqVO.getFeeSubsidy());
                adminLogService.insertLog(AdminOpTypeEnum.PLATFORM_TO_RENTER,renterCostReqVO.getOrderNo(),renterCostReqVO.getRenterOrderNo(),null,CompareBeanUtils.newInstance(oldData,newData).compare());
            }catch (Exception e){
        	    log.error("平台给租客的补贴修改日志记录异常",e);
            }
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updatePlatFormToRenterListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("updatePlatFormToRenterListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     * 200120 
     * @param renterCostReqVO
     * @param request
     * @param response
     * @param bindingResult
     * @return
     */
    @AutoDocMethod(description = "平台给车主的补贴 平台给车主的补贴修改", value = "平台给车主的补贴 平台给车主的补贴修改",response = ResponseData.class)
    @PostMapping("platFormToOwner/update")
    public ResponseData<?> updatePlatFormToOwnerListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.PlatformToOwnerSubsidyReqVO ownerCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("updatePlatFormToOwnerListByOrderNo controller params={}",ownerCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        ResponseData<PlatformToOwnerSubsidyDTO> responseData = null;
        try{
            responseData = ownerOrderDetailService.platformToOwnerSubsidy(ownerCostReqVO.getOrderNo(),ownerCostReqVO.getOwnerOrderNo());
        }catch (Exception e){
		    log.error("平台给车主的补贴-日志记录查询异常",e);
        }
        try {
        	orderCostDetailService.updatePlatFormToOwnerListByOrderNo(ownerCostReqVO);
        	try{
                PlatformToOwnerSubsidyDTO oldData = responseData.getData();
                PlatformToOwnerSubsidyDTO newData = new PlatformToOwnerSubsidyDTO();
                newData.setMileageAmt(Integer.valueOf(ownerCostReqVO.getMileageAmt()==null?"0":ownerCostReqVO.getMileageAmt()));
                newData.setOilSubsidyAmt(Integer.valueOf(ownerCostReqVO.getOilSubsidyAmt()==null?"0":ownerCostReqVO.getOilSubsidyAmt()));
                newData.setWashCarSubsidyAmt(Integer.valueOf(ownerCostReqVO.getWashCarSubsidyAmt()==null?"0":ownerCostReqVO.getWashCarSubsidyAmt()));
                newData.setCarGoodsLossSubsidyAmt(Integer.valueOf(ownerCostReqVO.getCarGoodsLossSubsidyAmt()==null?"0":ownerCostReqVO.getCarGoodsLossSubsidyAmt()));
                newData.setDelaySubsidyAmt(Integer.valueOf(ownerCostReqVO.getDelaySubsidyAmt()==null?"0":ownerCostReqVO.getDelaySubsidyAmt()));
                newData.setTrafficSubsidyAmt(Integer.valueOf(ownerCostReqVO.getTrafficSubsidyAmt()==null?"0":ownerCostReqVO.getTrafficSubsidyAmt()));
                newData.setIncomeSubsidyAmt(Integer.valueOf(ownerCostReqVO.getIncomeSubsidyAmt()==null?"0":ownerCostReqVO.getIncomeSubsidyAmt()));
                newData.setOtherSubsidyAmt(Integer.valueOf(ownerCostReqVO.getOtherSubsidyAmt()==null?"0":ownerCostReqVO.getOtherSubsidyAmt()));
                adminLogService.insertLog(AdminOpTypeEnum.PLATFORM_TO_OWNER,ownerCostReqVO.getOrderNo(),null,ownerCostReqVO.getOwnerOrderNo(),CompareBeanUtils.newInstance(oldData,newData).compare());
            }catch (Exception e){
        	    log.error("平台给车主的补贴操作异常",e);
            }
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updatePlatFormToOwnerListByOrderNo exception params="+ownerCostReqVO.toString(),e);
			logger.error("updatePlatFormToOwnerListByOrderNo exception params="+ownerCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    

    /**
     *租客车主互相调价  租客和车主都是调用同一个接口
     * @param priceAdjustmentVO
     * @return
     */
    @AutoDocMethod(description = "租客车主互相调价 车主租客互相调价操作", value = "租客车主互相调价 车主租客互相调价操作",response = ResponseData.class)
    @PostMapping("renterPriceAdjustment/update")
    public ResponseData<?> updateRenterPriceAdjustmentByOrderNo(@RequestBody RenterAdjustCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response, BindingResult bindingResult) {
    	logger.info("updateRenterPriceAdjustmentByOrderNo controller params={}",renterCostReqVO.toString());  //@Validated
        BindingResultUtil.checkBindingResult(bindingResult);
        RenterPriceAdjustmentResVO resp = null;
        try{
            RenterCostReqVO req = new RenterCostReqVO();
            req.setOrderNo(renterCostReqVO.getOrderNo());
            req.setOwnerOrderNo(renterCostReqVO.getOwnerOrderNo());
            req.setRenterOrderNo(renterCostReqVO.getRenterOrderNo());
            resp = orderCostDetailService.findRenterPriceAdjustmentByOrderNo(req);
        }catch (Exception e){
            log.error("租客车主互相调价-日志记录查询异常",e);
        }
        try {
        	/**
        	 * 全局补贴
        	 */
        	orderCostDetailService.updateRenterPriceAdjustmentByOrderNo(renterCostReqVO);
            try{
                if(StringUtils.isNotBlank(renterCostReqVO.getOwnerToRenterAdjustAmt())){
                    String oldData = resp.getOwnerToRenterAdjustAmt();
                    String desc = "车主给租客调价 由 " + oldData +" 修改为 " + renterCostReqVO.getOwnerToRenterAdjustAmt();
                    adminLogService.insertLog(AdminOpTypeEnum.RENTER_PRICE_ADJUSTMENT,renterCostReqVO.getOrderNo(),
                            renterCostReqVO.getRenterOrderNo(),renterCostReqVO.getOwnerOrderNo(),desc);
                }
                if(StringUtils.isNotBlank(renterCostReqVO.getRenterToOwnerAdjustAmt())){
                    String oldData = resp.getRenterToOwnerAdjustAmt();
                    String desc = "租客给车主的调价 由 " + oldData +" 修改为 " + renterCostReqVO.getRenterToOwnerAdjustAmt();
                    adminLogService.insertLog(AdminOpTypeEnum.RENTER_PRICE_ADJUSTMENT,renterCostReqVO.getOrderNo(),
                            renterCostReqVO.getRenterOrderNo(),renterCostReqVO.getOwnerOrderNo(),desc);
                }
            }catch (Exception e){
                log.error("租客车主互相调价 车主租客互相调价操作 日志记录 异常",e);
            }


        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updateRenterPriceAdjustmentByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("updateRenterPriceAdjustmentByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     *租客车主互相调价   租客和车主都是调用同一个接口
     * @param rentalCostReqVO
     * @return    PriceAdjustmentVO
     */
    @AutoDocMethod(description = "租客车主互相调价 车主租客互相调价展示", value = "租客车主互相调价 车主租客互相调价展示",response = RenterPriceAdjustmentResVO.class)
    @PostMapping("renterPriceAdjustment/list")
    public ResponseData<RenterPriceAdjustmentResVO> findRenterPriceAdjustmentByOrderNo(@RequestBody  RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findRenterPriceAdjustmentByOrderNo controller params={}",renterCostReqVO.toString());  //@Validated
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	RenterPriceAdjustmentResVO resp = orderCostDetailService.findRenterPriceAdjustmentByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findRenterPriceAdjustmentByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findRenterPriceAdjustmentByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    
    

    /**
     * 租客需支付给平台的费用
     * @param rentalCostReqVO
     * @return
     */
    @AutoDocMethod(description = "租客需支付给平台的费用 查询接口", value = "租客需支付给平台的费用 查询接口",response = RenterToPlatformVO.class)
    @PostMapping("renterToPlatForm/list")
    public ResponseData<RenterToPlatformVO> findRenterToPlatFormListByOrderNo(@RequestBody @Validated RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findRenterToPlatFormListByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	/**
        	 * 全局费用
        	 */
        	RenterToPlatformVO resp = orderCostDetailService.findRenterToPlatFormListByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findRenterToPlatFormListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findRenterToPlatFormListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    
    /**
     * 租客需支付给平台的费用
     * @param ownerToPlatFormVO
     * @return
     */
    @AutoDocMethod(description = "租客需支付给平台的费用 修改接口", value = "租客需支付给平台的费用 修改接口",response = ResponseData.class)
    @PostMapping("renterToPlatForm/update")
    public ResponseData<?> updateRenterToPlatFormListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.RenterToPlatformCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("updateRenterToPlatFormListByOrderNo controller params={}",renterCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        RenterToPlatformVO oldData = null;
        try{
            RenterCostReqVO req = new RenterCostReqVO();
            req.setOrderNo(renterCostReqVO.getOrderNo());
            req.setRenterOrderNo(renterCostReqVO.getRenterOrderNo());
            oldData = orderCostDetailService.findRenterToPlatFormListByOrderNo(req);
        }catch (Exception e){
            log.error("租客需支付给平台的费用-日志记录查询异常",e);
        }
        try {
        	orderCostDetailService.updateRenterToPlatFormListByOrderNo(renterCostReqVO);
        	try{
                RenterToPlatformVO newData = new RenterToPlatformVO();
                newData.setOliAmt(renterCostReqVO.getOliAmt());
                newData.setTimeOut(renterCostReqVO.getTimeOut());
                newData.setModifyOrderTimeAndAddrAmt(renterCostReqVO.getModifyOrderTimeAndAddrAmt());
                newData.setCarWash(renterCostReqVO.getCarWash());
                newData.setDlayWait(renterCostReqVO.getDlayWait());
                newData.setStopCar(renterCostReqVO.getStopCar());
                newData.setExtraMileage(renterCostReqVO.getExtraMileage());
                adminLogService.insertLog(AdminOpTypeEnum.RENTER_TO_PLATFORM,renterCostReqVO.getOrderNo(),
                        renterCostReqVO.getRenterOrderNo(),null, CompareBeanUtils.newInstance(oldData,newData).compare());
            }catch (Exception e){
        	    log.error("租客需支付给平台的费用日志记录异常",e);
            }


        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updateRenterToPlatFormListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("updateRenterToPlatFormListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    /**
     *租客租金明细, 参考车主的租金组成明细。
     * @param rentalCostReqVO
     * @return
     */
    @AutoDocMethod(description = "租客租金 租金明细接口", value = "租客租金 租金明细接口",response = RenterRentDetailDTO.class)
    @PostMapping("renterRentAmt/list")
    public ResponseData<RenterRentDetailDTO> findRenterRentAmtListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("findTenantRentListByOrderNo controller params={}",renterCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	RenterRentDetailDTO resp = orderCostDetailService.findRenterRentAmtListByOrderNo(renterCostReqVO);
        	return ResponseData.success(resp);
		} catch (Exception e) {
			Cat.logError("findRenterRentAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			logger.error("findRenterRentAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    // --------------------------------------------------------------------------------------------------- 第二阶段 

    
    /**
     * 车主需支付给平台的费用 @张斌
     * @param ownerToPlatFormVO
     * @return
     */
    @AutoDocMethod(description = "车主需支付给平台的费用 修改接口", value = "车主需支付给平台的费用 修改接口",response = ResponseData.class)
    @PostMapping("ownerToPlatForm/update")
    public ResponseData<?> updateOwnerToPlatFormListByOrderNo(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.OwnerToPlatformCostReqVO ownerCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("updateOwnerToPlatFormListByOrderNo controller params={}",ownerCostReqVO.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
        
        try {
        	orderCostDetailService.updateOwnerToPlatFormListByOrderNo(ownerCostReqVO);
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("updateOwnerToPlatFormListByOrderNo exception params="+ownerCostReqVO.toString(),e);
			logger.error("updateOwnerToPlatFormListByOrderNo exception params="+ownerCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }
    
    @AutoDocMethod(description = "车主给租客的租金补贴 修改接口", value = "车主给租客的租金补贴 修改接口",response = ResponseData.class)
    @PostMapping("ownerToRenterRentAmtSubsidy/update")
    public ResponseData<?> ownerToRenterRentAmtSubsidy(@RequestBody @Validated com.atzuche.order.commons.vo.rentercost.OwnerToRenterSubsidyReqVO ownerCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
    	logger.info("ownerToRenterRentAmtSubsidy controller params={}",ownerCostReqVO.toString());
        BindingResultUtil.checkBindingResult(bindingResult);
        List<OwnerOrderSubsidyDetailDTO> ownerOrderSubsidyDetailDTOS = null;
		try{
            ownerOrderSubsidyDetailDTOS = remoteFeignService.queryOwnerSubsidyByownerOrderNo(ownerCostReqVO.getOrderNo(), ownerCostReqVO.getOwnerOrderNo());
        }catch (Exception e){
		 log.error("车主给租客的租金补贴-日志记录查询异常",e);
        }
        try {
        	orderCostDetailService.ownerToRenterRentAmtSubsidy(ownerCostReqVO);
        	try{
                OwnerOrderSubsidyDetailDTO ownerOrderSubsidyDetailDTO = CostStatUtils.ownerSubsidtyFilterByCashNo(RenterCashCodeEnum.SUBSIDY_OWNER_TORENTER_RENTAMT.getCashNo(), ownerOrderSubsidyDetailDTOS);
                String desc = "租客租金补贴：";
                if(ownerOrderSubsidyDetailDTO == null){
                    desc += "将 0 修改为 "+ ownerCostReqVO.getOwnerSubsidyRentAmt();
                }else{
                    desc += "将 "+ownerOrderSubsidyDetailDTO.getSubsidyAmount()+" 修改为 "+ -Integer.valueOf(ownerCostReqVO.getOwnerSubsidyRentAmt());
                }
                adminLogService.insertLog(AdminOpTypeEnum.OWNER_TO_RENTER,ownerCostReqVO.getOrderNo(),null,ownerCostReqVO.getOwnerOrderNo(),desc);
            }catch (Exception e){
        	    log.error("车主给租客的租金补贴修改操作异常",e);
            }
        	return ResponseData.success();
		} catch (Exception e) {
			Cat.logError("ownerToRenterRentAmtSubsidy exception params="+ownerCostReqVO.toString(),e);
			logger.error("ownerToRenterRentAmtSubsidy exception params="+ownerCostReqVO.toString(),e);
			return ResponseData.error();
		}
    }


    @AutoDocMethod(description = "租客费用详情-弹窗", value = "租客费用详情-弹窗",response = RenterCostDetailDTO.class)
    @GetMapping("/renterOrderCostDetail")
    public ResponseData<RenterCostDetailDTO> renterOrderCostDetail(@RequestParam("orderNo") String orderNo) {
        logger.info("renterOrderCostDetail controller orderNo={}",orderNo);
        try {
            RenterCostDetailDTO renterCostDetailDTO = orderCostDetailService.renterOrderCostDetail(orderNo);
            return ResponseData.success(renterCostDetailDTO);
        } catch (Exception e) {
            Cat.logError("renterOrderCostDetail exception orderNo="+orderNo,e);
            logger.error("renterOrderCostDetail exception orderNo="+orderNo,e);
            return ResponseData.error();
        }
    }

    @AutoDocMethod(description = "获取出险次数", value = "获取出险次数",response = RenterCostDetailDTO.class)
    @GetMapping("/getDangerCount")
    public ResponseData<DangerCountRespVO> getDangerCount(@RequestParam("orderNo") String orderNo,@RequestParam("renterOrderNo") String renterOrderNo) {
        logger.info("renterOrderCostDetail controller orderNo={}，renterOrderNo={}",orderNo,renterOrderNo);
        DangerCountRespVO dangerCountRespVO = orderCostDetailService.getDangerCount(orderNo,renterOrderNo);
        return ResponseData.success(dangerCountRespVO);
    }
}
