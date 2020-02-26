package com.atzuche.order.renterwz.vo;

import java.io.Serializable;

/**
 * Created by wangcheng on 2014/7/29.
 */
public class HttpResult implements Serializable {

	private static final long serialVersionUID = -2047496549885637792L;
	
	private String resCode;
    private String resMsg;
    private Object data;
    
    public HttpResult() {
    	
    }
    
    public HttpResult(String resCode, String resMsg) {
    	this.resCode = resCode;
    	this.resMsg = resMsg;
    }

    public HttpResult(String resCode, String resMsg, Object data) {
        this.resCode = resCode;
        this.resMsg = resMsg;
        this.data = data;
    }

    public String getResCode() {
        return resCode;
    }
    public void setResCode(String resCode) {
        this.resCode = resCode;
    }
    public String getResMsg() {
        return resMsg;
    }
    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    
	@Override
	public String toString() {
		return "HttpResult [resCode=" + resCode + ", resMsg=" + resMsg + ", data=" + data + "]";
	}
    
}
