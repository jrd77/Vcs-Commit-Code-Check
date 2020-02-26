package com.atzuche.order.coreapi.submitOrder.exception;

import com.atzuche.order.commons.OrderException;
import com.autoyol.commons.web.ErrorCode;

/**
 * CancelOrderController 相关业务校验异常
 *
 * @author pengcheng.fu
 * @date 2019/2/4 15:21
 */
public class CancelOrderCheckException extends OrderException {

    public CancelOrderCheckException(String errorCode, String errorMsg) {
        super(errorCode, errorMsg);
    }

    public CancelOrderCheckException() {
        super(ErrorCode.TRANS_CANCEL_DUPLICATE.getCode(), ErrorCode.TRANS_CANCEL_DUPLICATE.getText());
    }

    public CancelOrderCheckException(ErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getText());
    }

}
