package com.atzuche.order.coin.service;


import com.atzuche.order.coin.entity.AccountRenterCostCoinEntity;
import com.atzuche.order.coin.mapper.AccountRenterCostCoinMapper;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * 租客订单使凹凸币流水
 *
 * @author ZhangBin
 * @date 2020-01-07 20:23:33
 */
@Service
public class AccountRenterCostCoinService {
    @Autowired
    private AccountRenterCostCoinMapper accountRenterCostCoinMapper;

    /**
     * 查询订单使用的凹凸币总金额
     * @return
     */
    public int getUserCoinAmtByOrder(String orderNo,String memNo){
        return accountRenterCostCoinMapper.getAccountRenterCostCoinTotal(orderNo);
    }

    public int getUserCoinTotalAmt(String memNo){
        //TODO:
        return 0;
    }
}
