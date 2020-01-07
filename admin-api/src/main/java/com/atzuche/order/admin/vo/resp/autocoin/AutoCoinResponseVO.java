package com.atzuche.order.admin.vo.resp.autocoin;

import com.autoyol.doc.annotation.AutoDocProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 用车的条件
 * 计算费用
 * @author xiaoxu.wang
 *
 *
 */
@Data
@ToString
public class AutoCoinResponseVO implements Serializable{
	@AutoDocProperty(value="钱包余额")
	private String balance;

}
