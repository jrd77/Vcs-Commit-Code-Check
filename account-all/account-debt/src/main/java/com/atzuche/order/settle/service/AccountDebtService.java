package com.atzuche.order.settle.service;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.atzuche.order.commons.enums.cashcode.RenterCashCodeEnum;
import com.atzuche.order.settle.entity.AccountDebtDetailEntity;
import com.atzuche.order.settle.service.notservice.AccountDebtDetailNoTService;
import com.atzuche.order.settle.service.notservice.AccountDebtNoTService;
import com.atzuche.order.settle.service.notservice.AccountDebtReceivableaDetailNoTService;
import com.atzuche.order.settle.vo.req.AccountDeductDebtReqVO;
import com.atzuche.order.settle.vo.req.AccountInsertDebtReqVO;
import com.atzuche.order.settle.vo.res.AccountDebtResVO;
import com.autoyol.commons.web.ErrorCode;


/**
 * 个人历史总额表
 *
 * @author ZhangBin
 * @date 2019-12-11 17:34:34
 */
@Service
public class AccountDebtService{
    @Autowired
    private AccountDebtNoTService accountDebtNoTService;
    @Autowired
    private AccountDebtDetailNoTService accountDebtDetailNoTService;
    @Autowired
    private AccountDebtReceivableaDetailNoTService accountDebtReceivableaDetailNoTService;


    /**
     * 根据会员号查询用户总欠款信息
     * @param memNo
     * @return
     */
    public AccountDebtResVO getAccountDebtByMemNo(String memNo) {
        return accountDebtNoTService.getAccountDebtByMemNo(memNo);
    }

    /**
     * 查看账户欠款总和
     * @param memNo
     * @return
     */
    public int getAccountDebtNumByMemNo(String memNo){
        AccountDebtResVO res = getAccountDebtByMemNo(memNo);
        if(Objects.isNull(res) || Objects.isNull(res.getDebtAmt())){
            return NumberUtils.INTEGER_ZERO;
        }
        return res.getDebtAmt();
    }

    /**
     * 抵扣历史欠款
     *  正数
     * @return
     */
    public int deductDebt(AccountDeductDebtReqVO accountDeductDebt) {
        // 1 参数校验
        Assert.notNull(accountDeductDebt, ErrorCode.PARAMETER_ERROR.getText());
        accountDeductDebt.check();
        // 2 查询用户所有待还的记录
        List<AccountDebtDetailEntity> accountDebtDetailAlls =  accountDebtDetailNoTService.getDebtListByMemNo(accountDeductDebt.getMemNo());
        //3 根据租客还款总额  从用户所有待还款记录中 过滤本次 待还款的记录
        List<AccountDebtDetailEntity> accountDebtDetails = accountDebtDetailNoTService.getDebtListByDebtAll(accountDebtDetailAlls,accountDeductDebt);
        //5更新欠款表 当前欠款数
        accountDebtDetailNoTService.updateAlreadyDeductDebt(accountDebtDetails);
        //6 记录欠款收款详情
        accountDebtReceivableaDetailNoTService.insertAlreadyReceivablea(accountDeductDebt.getAccountDebtReceivableaDetails());
        //7 更新总欠款表
        accountDebtNoTService.deductAccountDebt(accountDeductDebt);
        return accountDeductDebt.getRealAmt();
    }
    
    

    /**
     * 记录用户历史欠款
     */
    public int insertDebt(AccountInsertDebtReqVO accountInsertDebt){
        //1校验
        Assert.notNull(accountInsertDebt, ErrorCode.PARAMETER_ERROR.getText());
        accountInsertDebt.check();
        //2 查询账户欠款
        accountDebtNoTService.productAccountDebt(accountInsertDebt);
        //3 新增欠款明细
        return accountDebtDetailNoTService.insertDebtDetail(accountInsertDebt);

    }


}
