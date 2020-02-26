/**
 * 
 */
package com.atzuche.order.coreapi.controller;

import javax.validation.Valid;

import com.atzuche.order.accountrenterdeposit.entity.AccountRenterDepositEntity;
import com.atzuche.order.accountrenterwzdepost.entity.AccountRenterWzDepositEntity;
import com.atzuche.order.cashieraccount.service.CashierQueryService;
import com.atzuche.order.commons.BindingResultUtil;
import com.atzuche.order.commons.exceptions.OrderNotFoundException;
import com.atzuche.order.commons.exceptions.OwnerOrderNotFoundException;
import com.atzuche.order.commons.vo.res.RenterCostDetailVO;
import com.atzuche.order.open.vo.RenterCostShortDetailVO;
import com.atzuche.order.ownercost.entity.OwnerOrderEntity;
import com.atzuche.order.ownercost.service.OwnerOrderService;
import com.atzuche.order.parentorder.entity.OrderEntity;
import com.atzuche.order.parentorder.service.OrderService;
import com.atzuche.order.coreapi.service.RenterCostFacadeService;
import com.atzuche.order.renterorder.entity.RenterOrderEntity;
import com.atzuche.order.renterorder.service.RenterOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.atzuche.order.commons.vo.req.OrderCostReqVO;
import com.atzuche.order.commons.vo.res.OrderOwnerCostResVO;
import com.atzuche.order.commons.vo.res.OrderRenterCostResVO;
import com.atzuche.order.coreapi.service.OrderCostService;
import com.autoyol.commons.web.ResponseData;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jing.huang
 *
 */
@RestController
@Slf4j
public class OrderCostController {
	@Autowired
	private OrderCostService orderCostService;

	@Autowired
	private RenterCostFacadeService facadeService;
	@Autowired
	private OrderService orderService;

	@Autowired
	private RenterOrderService renterOrderService;

	@Autowired
	private OwnerOrderService ownerOrderService;

	@Autowired
	private CashierQueryService cashierQueryService;
	
	@PostMapping("/order/cost/renter/get")
	public ResponseData<OrderRenterCostResVO> orderCostRenterGet(@Valid @RequestBody OrderCostReqVO req, BindingResult bindingResult) {
		log.info("租客子订单费用详细 orderCostRenterGet params=[{}]", req.toString());
		BindingResultUtil.checkBindingResult(bindingResult);
		OrderRenterCostResVO resVo = orderCostService.orderCostRenterGet(req);
		return ResponseData.success(resVo);
	}

	@GetMapping("/order/renter/cost/fullDetail")
	public ResponseData<RenterCostDetailVO> getRenterCostFullDetail(@RequestParam("orderNo") String orderNo){
		OrderEntity orderEntity = orderService.getOrderEntity(orderNo);
		if(orderEntity==null){
			throw new OrderNotFoundException(orderNo);
		}
		String memNo = orderEntity.getMemNoRenter();
		RenterOrderEntity renterOrderEntity = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
		if(renterOrderEntity==null){
			throw new OrderNotFoundException(orderNo);
		}
		String renterOrderNo = renterOrderEntity.getRenterOrderNo();

		RenterCostDetailVO renterBasicCostDetailVO = facadeService.getRenterCostFullDetail(orderNo,renterOrderNo,memNo);

		return ResponseData.success(renterBasicCostDetailVO);
	}

	public ResponseData getCarOwnerIncomeFullDetail(@RequestParam("orderNo")String orderNo,@RequestParam("ownerNo")String ownerNo){
		OrderEntity orderEntity = orderService.getOrderEntity(orderNo);
		if(orderEntity==null){
			throw new OrderNotFoundException(orderNo);
		}

		OwnerOrderEntity ownerOrderEntity = ownerOrderService.getOwnerOrderByOrderNoAndIsEffective(orderNo);
		if(ownerOrderEntity==null){
			throw new OwnerOrderNotFoundException(orderNo);
		}

		if(!ownerNo.equals(ownerOrderEntity.getMemNo())){
           log.warn("查到的车主号码:{}和前端请求的车主号码:{}不一致",ownerOrderEntity.getMemNo(),ownerNo);
           throw new OwnerOrderNotFoundException(orderNo);
		}










		//FIXME:
        return null;
	}

	/**
	 * 获取租客的费用简况
	 * @param orderNo
	 * @return
	 */
	@GetMapping("/order/renter/cost/shortDetail")
	public ResponseData<RenterCostShortDetailVO> getRenterCostShortDetail(String orderNo){
         OrderEntity orderEntity = orderService.getOrderEntity(orderNo);
         if(orderEntity==null){
         	throw new OrderNotFoundException(orderNo);
		 }
         String memNo = orderEntity.getMemNoRenter();
		 RenterOrderEntity renterOrderEntity = renterOrderService.getRenterOrderByOrderNoAndIsEffective(orderNo);
		 if(renterOrderEntity==null){
			 throw new OrderNotFoundException(orderNo);
		 }
		 String renterOrderNo = renterOrderEntity.getRenterOrderNo();

		 int totalRentCostAmtWithoutFine = facadeService.getTotalRenterCostWithoutFine(orderNo,renterOrderNo,memNo);
		 int totalFineAmt = facadeService.getTotalFine(orderNo,renterOrderNo,memNo);

		AccountRenterDepositEntity depositEntity = cashierQueryService.getTotalToPayDepositAmt(orderNo);
		AccountRenterWzDepositEntity wzDepositEntity = cashierQueryService.getTotalToPayWzDepositAmt(orderNo);

		RenterCostShortDetailVO shortDetail = new RenterCostShortDetailVO();

		shortDetail.setTotalRentCostAmt(-totalRentCostAmtWithoutFine);
		shortDetail.setTotalFineAmt(-totalFineAmt);
		shortDetail.setYingFuDeposit(-depositEntity.getYingfuDepositAmt());
		shortDetail.setShiFuDeposit(depositEntity.getShifuDepositAmt());
		shortDetail.setYingFuWzDeposit(-wzDepositEntity.getYingshouDeposit());
		shortDetail.setShiFuWzDeposit(wzDepositEntity.getShishouDeposit());
		shortDetail.setToPayDeposit(-(depositEntity.getYingfuDepositAmt()+depositEntity.getShifuDepositAmt()));
		shortDetail.setToPayWzDeposit(-(wzDepositEntity.getYingshouDeposit()+wzDepositEntity.getShishouDeposit()));
		shortDetail.setExpReturnDeposit(depositEntity.getShifuDepositAmt());
		shortDetail.setExpReturnWzDeposit(wzDepositEntity.getShishouDeposit());
		shortDetail.setOrderNo(orderNo);

		return ResponseData.success(shortDetail);

	}


	
	@PostMapping("/order/cost/owner/get")
	public ResponseData<OrderOwnerCostResVO> orderCostOwnerGet(@Valid @RequestBody OrderCostReqVO req, BindingResult bindingResult) {
		log.info("车主子订单费用详细 orderCostOwnerGet params=[{}]", req.toString());
		BindingResultUtil.checkBindingResult(bindingResult);

		OrderOwnerCostResVO resVo = orderCostService.orderCostOwnerGet(req);
		return ResponseData.success(resVo);

	}


	
}
