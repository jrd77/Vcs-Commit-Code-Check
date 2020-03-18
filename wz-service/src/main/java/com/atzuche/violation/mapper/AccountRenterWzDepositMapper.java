package com.atzuche.violation.mapper;

import com.atzuche.violation.entity.AccountRenterWzDepositEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 违章押金状态及其总表
 * 
 * @author ZhangBin
 * @date 2019-12-11 17:58:17
 */
@Mapper
public interface AccountRenterWzDepositMapper {

    AccountRenterWzDepositEntity selectByPrimaryKey(Integer id);

    int insertSelective(AccountRenterWzDepositEntity record);
    
    int updateByPrimaryKeySelective(AccountRenterWzDepositEntity record);

    int updateByOrderNo(AccountRenterWzDepositEntity record);


    /**
     * 根据订单号查询车俩租金信息
     * @param orderNo
     * @param memNo
     * @return
     */
    AccountRenterWzDepositEntity selectByOrderAndMemNo(@Param("orderNo") String orderNo, @Param("memNo") String memNo);

    AccountRenterWzDepositEntity selectByOrder(@Param("orderNo") String orderNo);
}
