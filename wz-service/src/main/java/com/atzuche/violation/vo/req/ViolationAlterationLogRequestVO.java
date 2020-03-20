package com.atzuche.violation.vo.req;

import com.autoyol.doc.annotation.AutoDocProperty;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;


@Data
@ToString
public class ViolationAlterationLogRequestVO {
    @AutoDocProperty(value = "主订单号")
    @NotBlank(message = "主订单号不能为空")
    private String orderNo;

}
