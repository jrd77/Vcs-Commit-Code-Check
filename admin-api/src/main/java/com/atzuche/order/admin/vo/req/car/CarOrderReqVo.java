package com.atzuche.order.admin.vo.req.car;

import com.autoyol.doc.annotation.AutoDocProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CarOrderReqVo {
	@AutoDocProperty(value="orderNo,必填")
	@NotNull(message="订单号不能为空")
	private Integer orderNo;


}
