package com.atzuche.order.cashieraccount.exception;

import com.atzuche.order.commons.OrderException;
import com.atzuche.order.commons.enums.ErrorCode;
import lombok.Data;

@Data
public class OrderPaySignFailException extends OrderException {

    public OrderPaySignFailException() {
        super(ErrorCode.CASHIER_PAY_SIGN_PARAM_ERRER.getCode(), ErrorCode.CASHIER_PAY_SIGN_PARAM_ERRER.getCode());
    }
}
