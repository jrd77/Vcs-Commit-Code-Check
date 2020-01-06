package com.atzuche.order.cashieraccount.service;

import com.atzuche.order.accountownercost.entity.AccountOwnerCostSettleDetailEntity;
import com.atzuche.order.accountownercost.service.notservice.AccountOwnerCostSettleDetailNoTService;
import com.atzuche.order.accountownerincome.service.AccountOwnerIncomeService;
import com.atzuche.order.accountplatorm.entity.AccountPlatformProfitDetailEntity;
import com.atzuche.order.accountplatorm.entity.AccountPlatformSubsidyDetailEntity;
import com.atzuche.order.accountplatorm.service.notservice.AccountPlatformProfitDetailNotService;
import com.atzuche.order.accountplatorm.service.notservice.AccountPlatformSubsidyDetailNoTService;
import com.atzuche.order.accountrenterdeposit.service.AccountRenterDepositService;
import com.atzuche.order.accountrenterdeposit.vo.res.AccountRenterDepositResVO;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostDetailEntity;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostSettleDetailEntity;
import com.atzuche.order.accountrenterrentcost.entity.AccountRenterCostSettleEntity;
import com.atzuche.order.accountrenterrentcost.exception.AccountRenterRentCostSettleException;
import com.atzuche.order.accountrenterrentcost.service.AccountRenterCostSettleService;
import com.atzuche.order.accountrenterrentcost.service.notservice.AccountRenterCostSettleDetailNoTService;
import com.atzuche.order.accountrenterrentcost.service.notservice.AccountRenterCostSettleNoTService;
import com.atzuche.order.accountrenterwzdepost.service.AccountRenterWzDepositCostService;
import com.atzuche.order.accountrenterwzdepost.service.AccountRenterWzDepositService;
import com.atzuche.order.cashieraccount.service.notservice.CashierNoTService;
import com.atzuche.order.cashieraccount.service.notservice.CashierRefundApplyNoTService;
import com.atzuche.order.cashieraccount.vo.req.DeductDepositToRentCostReqVO;
import com.atzuche.order.commons.enums.RenterCashCodeEnum;
import com.atzuche.order.rentercost.service.RenterOrderCostCombineService;
import com.atzuche.order.settle.service.AccountDebtService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;


/**
 * 结算收银台操作
 *
 * @author ZhangBin
 * @date 2019-12-11 11:17:59
 */
@Service
public class CashierSettleService {
    @Autowired AccountRenterDepositService accountRenterDepositService;
    @Autowired AccountRenterWzDepositService accountRenterWzDepositService;
    @Autowired AccountDebtService accountDebtService;
    @Autowired CashierRefundApplyNoTService cashierRefundApplyNoTService;
    @Autowired AccountOwnerIncomeService accountOwnerIncomeService;
    @Autowired AccountRenterCostSettleService accountRenterCostSettleService;
    @Autowired AccountRenterWzDepositCostService accountRenterWzDepositCostService;
    @Autowired RenterOrderCostCombineService renterOrderCostCombineService;
    @Autowired CashierNoTService cashierNoTService;
    @Autowired AccountRenterCostSettleDetailNoTService accountRenterCostSettleDetailNoTService;
    @Autowired AccountOwnerCostSettleDetailNoTService accountOwnerCostSettleDetailNoTService;
    @Autowired AccountPlatformSubsidyDetailNoTService accountPlatformSubsidyDetailNoTService;
    @Autowired AccountPlatformProfitDetailNotService accountPlatformProfitDetailNotService;
    @Autowired private AccountRenterCostSettleNoTService accountRenterCostSettleNoTService;


    /**
     * 车辆结算
     * @param orderNo
     * @return
     */
    public List<AccountRenterCostDetailEntity> getAccountRenterCostDetailsByOrderNo(String orderNo){
        return accountRenterCostSettleService.getAccountRenterCostDetailsByOrderNo(orderNo);
    }

    /**
     * 车俩结算 租客费用明细落库
     * @param accountRenterCostSettleDetails
     */
    public void insertAccountRenterCostSettleDetails(List<AccountRenterCostSettleDetailEntity> accountRenterCostSettleDetails) {
        if(!CollectionUtils.isEmpty(accountRenterCostSettleDetails)){
            accountRenterCostSettleDetailNoTService.insertAccountRenterCostSettleDetails(accountRenterCostSettleDetails);
        }

    }

    /**
     * 车俩结算 车主费用明细落库
     * @param accountOwnerCostSettleDetails
     */
    public void insertAccountOwnerCostSettleDetails(List<AccountOwnerCostSettleDetailEntity> accountOwnerCostSettleDetails) {
        if(!CollectionUtils.isEmpty(accountOwnerCostSettleDetails)){
            accountOwnerCostSettleDetailNoTService.insertAccountOwnerCostSettleDetails(accountOwnerCostSettleDetails);
        }
    }

    /**
     * 车俩结算 车主费用明细落库
     * @param accountPlatformSubsidyDetails
     */
    public void insertAccountPlatformSubsidyDetails(List<AccountPlatformSubsidyDetailEntity> accountPlatformSubsidyDetails) {
        if(!CollectionUtils.isEmpty(accountPlatformSubsidyDetails)){
            accountPlatformSubsidyDetailNoTService.insertAccountPlatformSubsidyDetails(accountPlatformSubsidyDetails);
        }
    }

    /**
     * 平台收益明细 落库
     * @param accountPlatformProfitDetails
     */
    public void insertAccountPlatformProfitDetails(List<AccountPlatformProfitDetailEntity> accountPlatformProfitDetails) {
        if(!CollectionUtils.isEmpty(accountPlatformProfitDetails)){
            accountPlatformProfitDetailNotService.insertAccountPlatformProfitDetails(accountPlatformProfitDetails);
        }
    }

    /**
     * 计算租客 租车费用  平台补贴费用  车主补贴费用 手续费 基础保障费用 等 并落库
     * @param accountRenterCostSettleDetails
     */
    public AccountRenterCostSettleEntity updateRentSettleCost(String orderNo,String renterMemNo,List<AccountRenterCostSettleDetailEntity> accountRenterCostSettleDetails) {
        AccountRenterCostSettleEntity entity = accountRenterCostSettleNoTService.getCostPaidRentSettle(orderNo,renterMemNo);
        if(Objects.isNull(entity) || Objects.isNull(entity.getId())){
            throw new AccountRenterRentCostSettleException() ;
        }
        if(!CollectionUtils.isEmpty(accountRenterCostSettleDetails)){
            // 平台补贴费用
            int platformSubsidyAmount = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.ACCOUNT_CONSOLE_RENTER_SUBSIDY_COST.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //车主补贴费用
            int carOwnerSubsidyAmount = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.ACCOUNT_RENTER_SUBSIDY_COST.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //附加驾驶人保证费用
            int additionalDrivingEnsureAmount = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.EXTRA_DRIVER_INSURE.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //全面保障费用
            int comprehensiveEnsureAmount = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.ABATEMENT_INSURE.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //基础保障费用
            int basicEnsureAmount = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.INSURE_TOTAL_PRICES.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //手续费
            int yongjinAmt = accountRenterCostSettleDetails.stream().filter(obj ->{
                return RenterCashCodeEnum.FEE.getCashNo().equals(obj.getCostCode());
            }).mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            //租车费用
            int rentAmt = accountRenterCostSettleDetails.stream().mapToInt(AccountRenterCostSettleDetailEntity::getAmt).sum();
            entity.setYongjinAmt(yongjinAmt);
            entity.setRentAmt(rentAmt);
            entity.setPlatformSubsidyAmount(platformSubsidyAmount);
            entity.setCarOwnerSubsidyAmount(carOwnerSubsidyAmount);
            entity.setAdditionalDrivingEnsureAmount(additionalDrivingEnsureAmount);
            entity.setComprehensiveEnsureAmount(comprehensiveEnsureAmount);
            entity.setBasicEnsureAmount(basicEnsureAmount);
            accountRenterCostSettleNoTService.updateAccountRenterCostSettle(entity);

        }
        return entity;


    }

    /**
     * 结算返回租客 实付车辆押金
     * @param orderNo
     * @param renterMemNo
     * @return
     */
    public int getRentDeposit(String orderNo, String renterMemNo) {
        AccountRenterDepositResVO vo = accountRenterDepositService.getAccountRenterDepositEntity(orderNo,renterMemNo);
        if(Objects.isNull(vo) || Objects.isNull(vo.getOrderNo())){
            return NumberUtils.INTEGER_ZERO;
        }
        return vo.getSurplusDepositAmt();
    }


    /**
     * 结算时候， 应付金额大于实付金额，存在欠款，车辆押金抵扣
     */
    public void deductDepositToRentCost(DeductDepositToRentCostReqVO vo) {
    }
}
