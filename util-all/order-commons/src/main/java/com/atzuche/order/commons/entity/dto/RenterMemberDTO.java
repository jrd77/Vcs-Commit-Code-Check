package com.atzuche.order.commons.entity.dto;

import com.autoyol.member.detail.vo.res.CommUseDriverInfo;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RenterMemberDTO {
    /**
     * 主订单号
     */
    private String orderNo;
    /**
     * 子订单号
     */
    private String renterOrderNo;
    /**
     * 会员号
     */
    private String memNo;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 头像
     */
    private String headerUrl;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 驾驶证初次领证日期
     */
    private LocalDate certificationTime;
    /**
     * 成功下单次数
     */
    private Integer orderSuccessCount;
    /**
     * 是否可以租车：1：可租，0：不可租
     */
    private Integer rentFlag;
    /**
     * 姓
     */
    private String firstName;
    /**
     * 性别(1:男、2：女)
     */
    private Integer gender;
    /**
     * 身份证认证 0：未上传，1：已上传，2：已认证，3：认证不通过, 4:无效数据
     */
    private Integer idCardAuth;
    /**
     * 驾照认证 0：未上传，1：已上传，2：已认证，3：认证不通过, 4:无效数据
     */
    private Integer driLicAuth;
    /**
     * 驾驶增副页 0：未上传，1：已上传，2：已认证，3：认证不通过, 4:无效数据, 5:未上传（已认证）
     */
    private Integer driViceLicAuth;
    /**
     * 会员权益
     */
    private List<RenterMemberRightDTO> renterMemberRightDTOList;
    /**
     * 常用驾驶人列表
     */
    private List<CommUseDriverInfo> commUseDriverList;

    /**
     * 是否是新用户:0，否;1，是，定义没有已结算订单的用户为新用户
     */
    private Integer isNew;
    /**
     * 核查状态 0-未查核 1-核查通过 2-核查不通过 3-核查已通过，有劣迹
     */
    private Integer renterCheck;
}
