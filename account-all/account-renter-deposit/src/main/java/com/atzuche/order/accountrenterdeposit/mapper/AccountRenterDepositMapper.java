package com.atzuche.order.accountrenterdeposit.mapper;

import com.atzuche.order.accountrenterdeposit.entity.AccountRenterDepositEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 租车押金状态及其总表
 * 
 * @author ZhangBin
 * @date 2019-12-17 17:09:45
 */
@Mapper
public interface AccountRenterDepositMapper{

    AccountRenterDepositEntity selectByPrimaryKey(Integer id);

    int insertSelective(AccountRenterDepositEntity record);
    
    int updateByPrimaryKeySelective(AccountRenterDepositEntity record);

    /**
     * 根据订单号查询车俩租金信息
     * @param orderNo
     * @param memNo
     * @return
     */
    AccountRenterDepositEntity selectByOrderAndMemNo(@Param("orderNo") String orderNo, @Param("memNo")String memNo);
}
