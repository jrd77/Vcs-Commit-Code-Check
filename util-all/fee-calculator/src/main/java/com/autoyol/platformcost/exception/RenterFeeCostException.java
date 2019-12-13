package com.autoyol.platformcost.exception;

import com.autoyol.platformcost.enums.ExceptionCodeEnum;

public class RenterFeeCostException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8113487315085900322L;

	/**
	 * 错误编码
	 */
	private String resCode;
	/**
	 * 错误消息
	 */
	private String resMsg;
	
	public RenterFeeCostException(String resCode, String resMsg) {
		super(resMsg);
		this.resCode = resCode;
		this.resMsg = resMsg;
	}
	
	public RenterFeeCostException(ExceptionCodeEnum exceptionCodeEnum) {
		super(exceptionCodeEnum.getText());
		this.resCode = exceptionCodeEnum.getCode();
		this.resMsg = exceptionCodeEnum.getText();
    }


	public String getResCode() {
		return resCode;
	}

	public String getResMsg() {
		return resMsg;
	}
	
}
