/**
 * 
 */
package com.atzuche.order.coreapi.submitOrder.filter;

import com.atzuche.order.commons.entity.dto.OrderContextDTO;
import com.atzuche.order.coreapi.submitOrder.exception.SubmitOrderException;
import com.atzuche.order.request.NormalOrderReqVO;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * @Author ZhangBin
 * @Date 2019/12/12 15:29
 * @Description: 线程安全的过滤链
 * 
 **/
@Service
public class FilterChain {

	ThreadLocal<List<SubmitOrderFilter>> filters = new ThreadLocal<List<SubmitOrderFilter>>();

	public FilterChain() {
		super();
	}

	public FilterChain addFilterAll(List<SubmitOrderFilter> lst) {
		this.filters.set(lst);
		return this;
	}

	public void doFilter(NormalOrderReqVO submitReqDto, OrderContextDTO orderContextDto, FilterChain chain, int index) throws SubmitOrderException {
		if (index == filters.get().size()) {
			return;
		}
		SubmitOrderFilter submitOrderFilter = filters.get().get(index);
		index++;
        submitOrderFilter.doFilter(submitReqDto, orderContextDto, chain, index);
	}

}
