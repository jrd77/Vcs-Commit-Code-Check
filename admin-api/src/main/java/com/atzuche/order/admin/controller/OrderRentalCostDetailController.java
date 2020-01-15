/**
 * 
 */
package com.atzuche.order.admin.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atzuche.order.admin.service.OrderCostDetailService;
import com.atzuche.order.admin.vo.req.cost.RentalCostReqVO;
import com.atzuche.order.admin.vo.req.cost.RenterCostReqVO;
import com.atzuche.order.admin.vo.resp.cost.AdditionalDriverInsuranceVO;
import com.atzuche.order.admin.vo.resp.cost.PriceAdjustmentVO;
import com.atzuche.order.admin.vo.resp.income.OwnerToPlatFormVO;
import com.atzuche.order.admin.vo.resp.order.cost.detail.OrderRenterFineAmtDetailResVO;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.doc.annotation.AutoDocMethod;
import com.autoyol.doc.annotation.AutoDocVersion;
import com.dianping.cat.Cat;

/**
 * @author jing.huang
 *
 */
@AutoDocVersion(version = "管理后台租客租车费用 租客费用页面 相关明细接口文档")
@RestController
@RequestMapping("/console/ordercost/detail/")
public class OrderRentalCostDetailController {
	private static final Logger logger = LoggerFactory.getLogger(OrderRentalCostDetailController.class);
	
	@Autowired
	OrderCostDetailService orderCostDetailService;
//    /**
//     * 违约罚金
//     * @param ownerInComeReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "违约罚金 违约罚金明细", value = "违约罚金 违约罚金明细",response = OrderRenterFineAmtDetailResVO.class)
//    @PostMapping("/fineAmt/list")
//    public ResponseData<OrderRenterFineAmtDetailResVO> findfineAmtListByOrderNo(@RequestBody RenterCostReqVO renterCostReqVO, HttpServletRequest request, HttpServletResponse response,BindingResult bindingResult) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    
//    /**
//     *减免明细
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "减免明细 押金减免明细", value = "减免明细 押金减免明细",response = ResponseData.class)
//    @PostMapping("/reductionDetails/list")
//    public ResponseData<?> findReductionDetailsListByOrderNo(@RequestBody RenterCostReqVO renterCostReqVO) {
//    	logger.info("findReductionDetailsListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findReductionDetailsListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findReductionDetailsListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findReductionDetailsListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    
//    /**
//     *平台给租客的补贴
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "平台给租客的补贴 平台给租客的补贴明细", value = "平台给租客的补贴 平台给租客的补贴明细",response = ResponseData.class)
//    @PostMapping("/platFormToRenter/list")
//    public ResponseData<?> findPlatFormToRenterListByOrderNo(@RequestBody RentalCostReqVO rentalCostReqVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    
//    
//    /**
//     *租客租金明细
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "租客租金 租金明细接口", value = "租客租金 租金明细接口",response = ResponseData.class)
//    @PostMapping("/renterRentAmt/list")
//    public ResponseData<?> findTenantRentListByOrderNo(@RequestBody RentalCostReqVO rentalCostReqVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    
//    /**
//     *附加驾驶员险
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "附加驾驶员险 附加驾驶人明细", value = "附加驾驶员险 附加驾驶人明细",response = AdditionalDriverInsuranceVO.class)
//    @PostMapping("/additionalDriverInsurance/list")
//    public ResponseData<?> findAdditionalDriverInsuranceByOrderNo(@RequestBody RentalCostReqVO rentalCostReqVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//
//    /**
//     *附加驾驶员险
//     * @param additionalDriverInsuranceVO
//     * @return
//     */
//    @AutoDocMethod(description = "新增附加驾驶员险  新增附加驾驶人", value = "新增附加驾驶员险  新增附加驾驶人",response = ResponseData.class)
//    @PostMapping("/additionalDriverInsurance/add")
//    public ResponseData<?> insertAdditionalDriverInsuranceByOrderNo(@RequestBody AdditionalDriverInsuranceVO additionalDriverInsuranceVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    /**
//     *租客车主互相调价
//     * @param priceAdjustmentVO
//     * @return
//     */
//    @AutoDocMethod(description = "租客车主互相调价 车主租客互相调价操作", value = "租客车主互相调价 车主租客互相调价操作",response = ResponseData.class)
//    @PostMapping("/renterPriceAdjustment/update")
//    public ResponseData<?> updateRenterPriceAdjustmentByOrderNo(@RequestBody PriceAdjustmentVO priceAdjustmentVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    /**
//     *租客车主互相调价
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "租客车主互相调价 车主租客互相调价展示", value = "租客车主互相调价 车主租客互相调价展示",response = PriceAdjustmentVO.class)
//    @PostMapping("/renterPriceAdjustment/list")
//    public ResponseData<?> findRenterPriceAdjustmentByOrderNo(@RequestBody RentalCostReqVO rentalCostReqVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//    
//    
//    /**
//     * 租客需支付给平台的费用
//     * @param rentalCostReqVO
//     * @return
//     */
//    @AutoDocMethod(description = "租客需支付给平台的费用 查询接口", value = "租客需支付给平台的费用 查询接口",response = OwnerToPlatFormVO.class)
//    @PostMapping("/renterToPlatForm/list")
//    public ResponseData<?> findRenterToPlatFormListByOrderNo(@RequestBody RentalCostReqVO rentalCostReqVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
//
//    /**
//     * 租客需支付给平台的费用
//     * @param ownerToPlatFormVO
//     * @return
//     */
//    @AutoDocMethod(description = "租客需支付给平台的费用 修改接口", value = "租客需支付给平台的费用 修改接口",response = OwnerToPlatFormVO.class)
//    @PostMapping("/ownerToPlatForm/update")
//    public ResponseData<?> updateRenterToPlatFormListByOrderNo(@RequestBody OwnerToPlatFormVO ownerToPlatFormVO) {
//    	logger.info("findfineAmtListByOrderNo controller params={}",renterCostReqVO.toString());
//		if (bindingResult.hasErrors()) {
//            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), ErrorCode.INPUT_ERROR.getText());
//        }
//        
//        try {
//        	OrderRenterFineAmtDetailResVO resp = orderCostDetailService.findfineAmtListByOrderNo(renterCostReqVO);
//        	return ResponseData.success(resp);
//		} catch (Exception e) {
//			Cat.logError("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			logger.error("findfineAmtListByOrderNo exception params="+renterCostReqVO.toString(),e);
//			return ResponseData.error();
//		}
//    }
    

}
