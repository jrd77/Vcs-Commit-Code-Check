package com.atzuche.order.coreapi.controller;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.atzuche.order.coreapi.entity.request.ModifyApplyHandleReq;
import com.atzuche.order.coreapi.entity.request.ModifyOrderReq;
import com.atzuche.order.coreapi.service.ModifyOrderService;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;

@RestController
public class ModifyOrderController {

	@Autowired
	private ModifyOrderService modifyOrderService;
	
	/**
	 * 修改订单
	 * @param modifyOrderReq
	 * @param bindingResult
	 * @return ResponseData
	 */
	@PostMapping("/order/modify")
	public ResponseData<?> modifyOrder(@Valid @RequestBody ModifyOrderReq modifyOrderReq, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
            Optional<FieldError> error = bindingResult.getFieldErrors().stream().findFirst();
            return new ResponseData<>(ErrorCode.INPUT_ERROR.getCode(), error.isPresent() ?
                    error.get().getDefaultMessage() : ErrorCode.INPUT_ERROR.getText());
        }
		return modifyOrderService.modifyOrder(modifyOrderReq);
	}
	
	/**
	 * 车主处理修改申请
	 * @param modifyApplyHandleReq
	 * @param bindingResult
	 * @return ResponseData
	 */
	@PostMapping("/order/modifyconfirm")
	public ResponseData<?> ownerHandleModifyApplication(@Valid @RequestBody ModifyApplyHandleReq modifyApplyHandleReq, BindingResult bindingResult) {
		return null;
	}
}
