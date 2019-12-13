package com.atzuche.order.mapper;

import com.atzuche.order.entity.AccountDebtEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 个人历史总额表
 * 
 * @author ZhangBin
 * @date 2019-12-11 17:34:34
 */
@Mapper
public interface AccountDebtMapper{

    AccountDebtEntity selectByPrimaryKey(Integer id);

    int insert(AccountDebtEntity record);
    
    int insertSelective(AccountDebtEntity record);

    int updateByPrimaryKeySelective(AccountDebtEntity record);

    /**根据会员号查询个人总欠款信息
     * @param memNo
     * @return
     */
    AccountDebtEntity getAccountDebtByMemNo(@Param("memNo")Integer memNo);
}
