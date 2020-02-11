package com.atzuche.order.accountplatorm.vo.req;

import com.atzuche.order.commons.enums.SubsidySourceCodeEnum;
import com.atzuche.order.commons.enums.cashcode.RenterCashCodeEnum;
import com.autoyol.commons.web.ErrorCode;
import lombok.Data;
import org.springframework.util.Assert;

/**
 *  订单补贴信息
 * @author haibao.yan
 */
@Data
public class AccountPlatformSubsidyDetailReqVO {

    /**
     * 主订单号
     */
    private String orderNo;
    /**
     * 费用
     */
    private int amt;
    /**
     * 费来源编码
     */
    private RenterCashCodeEnum renterCashCodeEnum;
    /**
     * 补贴产生凭证
     */
    private String uniqueNo;
    /**
     * 补贴方（车主/租客）
     */
    private SubsidySourceCodeEnum subsidyName;

    /**
     * 创建人
     */
    private String createOp;

    /**
     * 修改人
     */
    private String updateOp;


    public void check() {
        Assert.notNull(getOrderNo(), ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(getAmt(), ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(getRenterCashCodeEnum(), ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(getUniqueNo(), ErrorCode.PARAMETER_ERROR.getText());
        Assert.notNull(getSubsidyName(), ErrorCode.PARAMETER_ERROR.getText());
    }
}
