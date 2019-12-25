package com.atzuche.order.coreapi.service;

import com.alibaba.fastjson.JSON;
import com.atzuche.order.commons.CatConstants;
import com.atzuche.order.commons.LocalDateTimeUtils;
import com.atzuche.order.commons.entity.dto.OwnerMemberDTO;
import com.atzuche.order.commons.entity.dto.OwnerMemberRightDTO;
import com.atzuche.order.commons.entity.dto.RenterMemberDTO;
import com.atzuche.order.commons.entity.dto.RenterMemberRightDTO;
import com.atzuche.order.commons.enums.MemberFlagEnum;
import com.atzuche.order.commons.enums.OwnerMemRightEnum;
import com.atzuche.order.commons.enums.RenterMemRightEnum;
import com.atzuche.order.coreapi.enums.SubmitOrderErrorEnum;
import com.atzuche.order.coreapi.submitOrder.exception.OwnerberByFeignException;
import com.atzuche.order.coreapi.submitOrder.exception.RenterMemberByFeignException;
import com.autoyol.commons.web.ErrorCode;
import com.autoyol.commons.web.ResponseData;
import com.autoyol.member.detail.api.MemberDetailFeignService;
import com.autoyol.member.detail.enums.MemberSelectKeyEnum;
import com.autoyol.member.detail.vo.res.*;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 封装对远程会员详情服务的相关调用
 * @author <a href="mailto:lianglin.sjtu@gmail.com">AndySjtu</a>
 * @date 2019/12/19 2:18 下午
 **/
@Service
public class MemberService {
    private final static Logger log = LoggerFactory.getLogger(MemberService.class);
    
    @Autowired
    private MemberDetailFeignService memberDetailFeignService;

    //获取车主会员信息
    public OwnerMemberDTO getOwnerMemberInfo(String memNo) throws RenterMemberByFeignException {
        List<String> selectKey = Arrays.asList(
                MemberSelectKeyEnum.MEMBER_CORE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_BASE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ROLE_INFO.getKey());
        ResponseData<MemberTotalInfo> responseData = null;
        log.info("Feign 开始获取车主会员信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberSelectInfo(Integer.valueOf(memNo), selectKey);
            Cat.logEvent(CatConstants.FEIGN_RESULT,JSON.toJSONString(responseData));
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode())){
                log.error("Feign 获取车主会员信息失败,memNo={},orderContextDto={}",memNo, JSON.toJSONString(responseData));
                OwnerberByFeignException ownerberByFeignException = new OwnerberByFeignException(SubmitOrderErrorEnum.FEIGN_GET_OWNER_MEMBER_FAIL.getCode(), SubmitOrderErrorEnum.FEIGN_GET_OWNER_MEMBER_FAIL.getText());
                Cat.logError("Feign 获取车主会员信息失败",ownerberByFeignException);
                t.setStatus(ownerberByFeignException);
                throw ownerberByFeignException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch (Exception e){
            t.setStatus(e);
            Cat.logError("Feign 获取车主会员信息失败",e);
            log.error("Feign 获取车主会员信息失败,orderContextDto={},memNo={}",memNo,e);
            throw new OwnerberByFeignException(SubmitOrderErrorEnum.FEIGN_GET_OWNER_MEMBER_ERROR.getCode(),SubmitOrderErrorEnum.FEIGN_GET_OWNER_MEMBER_ERROR.getText());
        }finally {
            t.complete();
        }


        MemberTotalInfo memberTotalInfo = responseData.getData();
        MemberCoreInfo memberCoreInfo = memberTotalInfo.getMemberCoreInfo();
        OwnerMemberDTO ownerMemberDto = new OwnerMemberDTO();
        ownerMemberDto.setMemNo(memNo);
        ownerMemberDto.setPhone(memberCoreInfo.getPhone());
        ownerMemberDto.setHeaderUrl(memberCoreInfo.getPortraitPath());
        ownerMemberDto.setRealName(memberCoreInfo.getRealName());
        ownerMemberDto.setNickName(memberCoreInfo.getNickName());
        List<OwnerMemberRightDTO> rights = new ArrayList<>();
        MemberRoleInfo memberRoleInfo = memberTotalInfo.getMemberRoleInfo();

        if(memberRoleInfo != null){
            if(memberRoleInfo.getInternalStaff()!=null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.STAFF.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.STAFF.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getInternalStaff()));
                internalStaff.setRightDesc("是否是内部员工");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getMemberFlag() != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                MemberFlagEnum memberFlagEnum = MemberFlagEnum.getRightByIndex(memberRoleInfo.getMemberFlag());
                internalStaff.setRightCode(memberFlagEnum.getRightCode());
                internalStaff.setRightName(memberFlagEnum.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getMemberFlag()));
                internalStaff.setRightDesc("会员标识");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getCpicMemberFlag() != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.CPIC_MEM.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.CPIC_MEM.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getCpicMemberFlag()));
                internalStaff.setRightDesc("是否太保会员");
                rights.add(internalStaff);
            }
        }
        MemberReliefInfo memberReliefInfo = memberTotalInfo.getMemberReliefInfo();
        if(memberReliefInfo != null){
            WxBindingTaskInfo wxBindingTaskInfo = memberReliefInfo.getWxBindingTaskInfo();
            if(wxBindingTaskInfo !=null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.BIND_WECHAT.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.BIND_WECHAT.getRightName());
                internalStaff.setRightValue(wxBindingTaskInfo.getReliefPercentage()==null?"0":String.valueOf(wxBindingTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(wxBindingTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            MemberLevelTaskInfo memberLevelTaskInfo = memberReliefInfo.getMemberLevelTaskInfo();
            if(memberLevelTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.MEMBER_LEVEL.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.MEMBER_LEVEL.getRightName());
                internalStaff.setRightValue(memberLevelTaskInfo.getReliefPercentage()==null?"0":String.valueOf(memberLevelTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(memberLevelTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            InvitationTaskInfo invitationTaskInfo = memberReliefInfo.getInvitationTaskInfo();
            if(invitationTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.INVITE_FRIENDS.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.INVITE_FRIENDS.getRightName());
                internalStaff.setRightValue(invitationTaskInfo.getReliefPercentage()==null?"0":String.valueOf(invitationTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(invitationTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            RentCarTaskInfo rentCarTaskInfo = memberReliefInfo.getRentCarTaskInfo();
            if(rentCarTaskInfo != null){
                OwnerMemberRightDTO internalStaff = new OwnerMemberRightDTO();
                internalStaff.setRightCode(OwnerMemRightEnum.SUCCESS_RENTCAR.getRightCode());
                internalStaff.setRightName(OwnerMemRightEnum.SUCCESS_RENTCAR.getRightName());
                internalStaff.setRightValue(rentCarTaskInfo.getReliefPercentage()==null?"0":String.valueOf(rentCarTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(rentCarTaskInfo.getTitle());
                rights.add(internalStaff);
            }
        }
        ownerMemberDto.setOwnerMemberRightDTOList(rights);
        return ownerMemberDto;
    }

    //获取租客会员信息
    public RenterMemberDTO getRenterMemberInfo(String memNo) throws RenterMemberByFeignException {
        List<String> selectKey = Arrays.asList(
                MemberSelectKeyEnum.MEMBER_CORE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_AUTH_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_BASE_INFO.getKey(),
                MemberSelectKeyEnum.MEMBER_ROLE_INFO.getKey());
        ResponseData<MemberTotalInfo> responseData = null;
        log.info("Feign 开始获取租客会员信息,memNo={}",memNo);
        Transaction t = Cat.newTransaction(CatConstants.FEIGN_CALL, "会员详情服务");
        try{
            Cat.logEvent(CatConstants.FEIGN_METHOD,"MemberDetailFeignService.getMemberSelectInfo");
            String parameter = "memNo="+memNo+"&selectKey"+JSON.toJSONString(selectKey);
            Cat.logEvent(CatConstants.FEIGN_PARAM,parameter);
            responseData = memberDetailFeignService.getMemberSelectInfo(Integer.parseInt(memNo), selectKey);
            if(responseData == null || !ErrorCode.SUCCESS.getCode().equals(responseData.getResCode()) || responseData.getData() == null){
                log.error("Feign 获取租客会员信息失败,memNo={},responseData={}",memNo,JSON.toJSONString(responseData));
                RenterMemberByFeignException renterMemberByFeignException = new RenterMemberByFeignException(SubmitOrderErrorEnum.FEIGN_GET_RENTER_MEMBER_FAIL.getCode(), SubmitOrderErrorEnum.FEIGN_GET_RENTER_MEMBER_FAIL.getText());
                Cat.logError("Feign 获取租客会员信息失败",renterMemberByFeignException);
                throw renterMemberByFeignException;
            }
            t.setStatus(Transaction.SUCCESS);
        }catch (Exception e){
            log.error("Feign 获取租客会员信息失败,submitReqDto={},memNo={}",memNo,null,e);
            RenterMemberByFeignException renterMemberByFeignException = new RenterMemberByFeignException(SubmitOrderErrorEnum.FEIGN_GET_RENTER_MEMBER_ERROR.getCode(), SubmitOrderErrorEnum.FEIGN_GET_RENTER_MEMBER_ERROR.getText());
            Cat.logError("Feign 获取租客会员信息失败",renterMemberByFeignException);
            t.setStatus(e);
            throw renterMemberByFeignException;
        }finally {
            t.complete();
        }

        MemberTotalInfo memberTotalInfo = responseData.getData();
        MemberAuthInfo memberAuthInfo = memberTotalInfo.getMemberAuthInfo();
        MemberCoreInfo memberCoreInfo = memberTotalInfo.getMemberCoreInfo();
        MemberBaseInfo memberBaseInfo = memberTotalInfo.getMemberBaseInfo();
        RenterMemberDTO renterMemberDto = new RenterMemberDTO();
        renterMemberDto.setMemNo(memNo);
        renterMemberDto.setPhone(memberCoreInfo.getPhone());
        renterMemberDto.setHeaderUrl(memberCoreInfo.getPortraitPath());
        renterMemberDto.setRealName(memberCoreInfo.getRealName());
        renterMemberDto.setNickName(memberCoreInfo.getNickName());
        renterMemberDto.setCertificationTime(LocalDateTimeUtils.parseStringToLocalDate(memberAuthInfo.getDriLicFirstTime()));
        renterMemberDto.setRentFlag(memberCoreInfo.getRentFlag());
        renterMemberDto.setFirstName(memberBaseInfo.getFirstName());
        renterMemberDto.setGender(memberBaseInfo.getGender());
        renterMemberDto.setIdCardAuth(memberAuthInfo.getIdCardAuth());
        renterMemberDto.setDriLicAuth(memberAuthInfo.getDriLicAuth());
        renterMemberDto.setDriViceLicAuth(memberAuthInfo.getDriViceLicAuth());
        //renterMemberDto.setOrderSuccessCount();
        List<RenterMemberRightDTO> rights = new ArrayList<>();
        MemberRoleInfo memberRoleInfo = memberTotalInfo.getMemberRoleInfo();
        if(memberRoleInfo != null){
            if(memberRoleInfo.getInternalStaff()!=null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.STAFF.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.STAFF.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getInternalStaff()));
                internalStaff.setRightDesc("是否是内部员工");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getMemberFlag() != null){
                MemberFlagEnum memberFlagEnum = MemberFlagEnum.getRightByIndex(memberRoleInfo.getMemberFlag());
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(memberFlagEnum.getRightCode());
                internalStaff.setRightName(memberFlagEnum.getRightName());
                internalStaff.setRightDesc("会员标识");
                rights.add(internalStaff);
            }
            if(memberRoleInfo.getCpicMemberFlag() != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.CPIC_MEM.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.CPIC_MEM.getRightName());
                internalStaff.setRightValue(String.valueOf(memberRoleInfo.getCpicMemberFlag()));
                internalStaff.setRightDesc("是否太保会员");
                rights.add(internalStaff);
            }
        }
        MemberReliefInfo memberReliefInfo = memberTotalInfo.getMemberReliefInfo();
        if(memberReliefInfo != null){
            WxBindingTaskInfo wxBindingTaskInfo = memberReliefInfo.getWxBindingTaskInfo();
            if(wxBindingTaskInfo !=null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.BIND_WECHAT.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.BIND_WECHAT.getRightName());
                internalStaff.setRightValue(wxBindingTaskInfo.getReliefPercentage()==null?"0":String.valueOf(wxBindingTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(wxBindingTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            MemberLevelTaskInfo memberLevelTaskInfo = memberReliefInfo.getMemberLevelTaskInfo();
            if(memberLevelTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.MEMBER_LEVEL.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.MEMBER_LEVEL.getRightName());
                internalStaff.setRightValue(memberLevelTaskInfo.getReliefPercentage()==null?"0":String.valueOf(memberLevelTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(memberLevelTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            InvitationTaskInfo invitationTaskInfo = memberReliefInfo.getInvitationTaskInfo();
            if(invitationTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.INVITE_FRIENDS.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.INVITE_FRIENDS.getRightName());
                internalStaff.setRightValue(invitationTaskInfo.getReliefPercentage()==null?"0":String.valueOf(invitationTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(invitationTaskInfo.getTitle());
                rights.add(internalStaff);
            }
            RentCarTaskInfo rentCarTaskInfo = memberReliefInfo.getRentCarTaskInfo();
            if(rentCarTaskInfo != null){
                RenterMemberRightDTO internalStaff = new RenterMemberRightDTO();
                internalStaff.setRightCode(RenterMemRightEnum.SUCCESS_RENTCAR.getRightCode());
                internalStaff.setRightName(RenterMemRightEnum.SUCCESS_RENTCAR.getRightName());
                internalStaff.setRightValue(rentCarTaskInfo.getReliefPercentage()==null?"0":String.valueOf(rentCarTaskInfo.getReliefPercentage()));
                internalStaff.setRightDesc(rentCarTaskInfo.getTitle());
                rights.add(internalStaff);
            }
        }
        renterMemberDto.setRenterMemberRightDTOList(rights);
        return renterMemberDto;
    }

}
