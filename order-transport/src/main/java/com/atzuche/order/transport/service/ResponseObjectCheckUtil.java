package com.atzuche.order.transport.service;

import com.atzuche.order.commons.constant.OrderConstant;
import com.atzuche.order.commons.exceptions.RemoteCallException;
import com.autoyol.car.api.model.vo.ResponseObject;
import com.autoyol.commons.web.ErrorCode;
import org.springframework.util.ObjectUtils;

/**
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2020/1/21 10:35 上午
 **/
public class ResponseObjectCheckUtil {
    public static void  checkResponse(ResponseObject responseObject){
        if(responseObject==null||!ErrorCode.SUCCESS.getCode().equalsIgnoreCase(responseObject.getResCode())){
            RemoteCallException remoteCallException = null;
            if(responseObject!=null){
                remoteCallException = new RemoteCallException(responseObject.getResCode(),responseObject.getResMsg(),responseObject.getData());
            }else{
                remoteCallException = new RemoteCallException(com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getCode(),
                        com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getText());
            }
            throw remoteCallException;
        }
    }



    public static void checkCarDispatchResponse(ResponseObject responseObject){
        if(ObjectUtils.isEmpty(responseObject)) {
            throw new RemoteCallException(com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getCode(),
                    com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getText());
        }

        if(!ErrorCode.SUCCESS.getCode().equalsIgnoreCase(responseObject.getResCode()) &&
                !responseObject.getResCode().startsWith(OrderConstant.SPECIAL_ERROR_CODE_PREFIX)){
            throw new RemoteCallException(com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getCode(),
                        com.atzuche.order.commons.enums.ErrorCode.REMOTE_CALL_FAIL.getText());
        }
    }
}
