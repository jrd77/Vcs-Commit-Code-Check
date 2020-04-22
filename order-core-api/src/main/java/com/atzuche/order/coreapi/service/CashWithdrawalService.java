package com.atzuche.order.coreapi.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atzuche.order.accountownercost.entity.AccountOwnerCostSettleDetailEntity;
import com.atzuche.order.accountownercost.service.notservice.AccountOwnerCostSettleDetailNoTService;
import com.atzuche.order.accountownerincome.entity.AccountOwnerIncomeEntity;
import com.atzuche.order.accountownerincome.service.notservice.AccountOwnerIncomeNoTService;
import com.atzuche.order.cashieraccount.entity.AccountOwnerCashExamine;
import com.atzuche.order.cashieraccount.service.AccountOwnerCashExamineService;
import com.atzuche.order.cashieraccount.service.RemoteAccountService;
import com.atzuche.order.commons.entity.dto.CashWithdrawalSimpleMemberDTO;
import com.atzuche.order.commons.entity.dto.SearchCashWithdrawalReqDTO;
import com.atzuche.order.commons.enums.cashcode.OwnerCashCodeEnum;
import com.atzuche.order.commons.vo.req.AccountOwnerCashExamineReqVO;
import com.atzuche.order.coreapi.entity.vo.OwnerGpsDeductVO;
import com.atzuche.order.mem.MemProxyService;
import com.atzuche.order.owner.commodity.service.OwnerGoodsService;
import com.atzuche.order.wallet.api.MemBalanceVO;
import com.autoyol.platformcost.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CashWithdrawalService {

	@Autowired
	private AccountOwnerCashExamineService accountOwnerCashExamineService;
	@Autowired
	private MemProxyService memProxyService;
	@Autowired
	private AccountOwnerIncomeNoTService accountOwnerIncomeNoTService;
	@Autowired
	private RemoteAccountService remoteAccountService;
	@Autowired
	private OwnerGoodsService ownerGoodsService;
	@Autowired
	private AccountOwnerCostSettleDetailNoTService accountOwnerCostSettleDetailNoTService;
	
	/**
	 * 提现功能
	 * @param req
	 */
	@Transactional(rollbackFor=Exception.class)
	public void cashWithdrawal(AccountOwnerCashExamineReqVO req) {
		log.info("提现开始CashWithdrawalService.cashWithdrawal accountOwnerCashExamineReqVO=[{}]",req);
		// 获取会员信息
		CashWithdrawalSimpleMemberDTO simpleMem = memProxyService.getSimpleMemberInfo(req.getMemNo());
		// 提现主逻辑
		accountOwnerCashExamineService.saveAccountOwnerCashExamine(req, simpleMem);
	}
	
	
	/**
	 * 获取新提现列表根据会员号
	 * @param req
	 * @return List<AccountOwnerCashExamine>
	 */
	public List<AccountOwnerCashExamine> listCashWithdrawal(SearchCashWithdrawalReqDTO req) {
		log.info("获取新提现列表根据会员号CashWithdrawalService.listCashWithdrawal searchCashWithdrawalReqDTO=[{}]",req);
		Integer memNo = StringUtils.isBlank(req.getMemNo()) ? null:Integer.valueOf(req.getMemNo());
		return accountOwnerCashExamineService.listAccountOwnerCashExamineByMemNo(memNo);
	}
	
	
	/**
	 * 获取可提现金额
	 * @param req
	 * @return Integer
	 */
	public Integer getBalance(SearchCashWithdrawalReqDTO req) {
		log.info("获取可提现金额CashWithdrawalService.getBalance searchCashWithdrawalReqDTO=[{}]",req);
		// 调远程获取老系统可提现余额
		MemBalanceVO memBalanceVO = remoteAccountService.getMemBalance(req.getMemNo());
		// 获取新订单系统的会员总收益
		AccountOwnerIncomeEntity incomeEntity = accountOwnerIncomeNoTService.getOwnerIncome(req.getMemNo());
		int balance = 0;
		if (memBalanceVO != null && memBalanceVO.getBalance() != null && memBalanceVO.getBalance() > 0) {
			balance += memBalanceVO.getBalance();
		}
		if (incomeEntity != null && incomeEntity.getIncomeAmt() != null) {
			balance += incomeEntity.getIncomeAmt();
		}
		return balance;
	}
	
	
	/**
	 * 获取车主gps押金抵扣记录
	 * @param memNo
	 * @param carNo
	 * @return List<OwnerGpsDeductVO>
	 */
	public List<OwnerGpsDeductVO> listOwnerGpsDeduct(String memNo, Integer carNo) {
		List<String> orderNoList = ownerGoodsService.listOrderNoByCarNo(carNo);
		if (orderNoList == null || orderNoList.isEmpty()) {
			return null;
		}
		List<AccountOwnerCostSettleDetailEntity> ownerCostSettleList = accountOwnerCostSettleDetailNoTService.listOwnerSettleCostBySourceCode(orderNoList, memNo, OwnerCashCodeEnum.HW_DEPOSIT_DEBT.getCashNo());
		if (ownerCostSettleList == null || ownerCostSettleList.isEmpty()) {
			return null;
		}
		List<OwnerGpsDeductVO> list = new ArrayList<OwnerGpsDeductVO>();
		for (AccountOwnerCostSettleDetailEntity ocs:ownerCostSettleList) {
			OwnerGpsDeductVO ownerGpsDeductVO = new OwnerGpsDeductVO();
			ownerGpsDeductVO.setDeposit(ocs.getAmt() == null?null:String.valueOf(ocs.getAmt()));
			ownerGpsDeductVO.setGpsDepositTxt("GPS押金");
			ownerGpsDeductVO.setIsEnterTransFlag("1");
			ownerGpsDeductVO.setOrderNo(ocs.getOrderNo());
			ownerGpsDeductVO.setSettleDate(CommonUtils.formatTime(ocs.getCreateTime(), "yyyy.MM.dd"));
			list.add(ownerGpsDeductVO);
		}
		return list;
	}
}
