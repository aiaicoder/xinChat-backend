package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserContactApplyMapper;
import com.xin.xinChat.mapper.UserContactMapper;
import com.xin.xinChat.model.dto.apply.ApplyAddRequest;
import com.xin.xinChat.model.dto.apply.ApplyDealRequest;
import com.xin.xinChat.model.dto.apply.ApplyQueryRequest;
import com.xin.xinChat.model.dto.search.UserSearchRequest;
import com.xin.xinChat.model.entity.UserContactApply;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.UserContactApplyService;
import com.xin.xinChat.service.UserContactService;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/2 20:18
 */
@RestController
@RequestMapping("/userContact")
public class UserContactController {

    @Resource
    private UserContactService userContactService;


    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactApplyService userContactApplyService;

    @PostMapping("/search")
    @SaCheckLogin
    public BaseResponse<UserSearchVo> search(@RequestBody UserSearchRequest userSearchRequest) {
        if (userSearchRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userSearchRequest.getUserId() == null || userSearchRequest.getContactId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String userId = userSearchRequest.getUserId();
        String contactId = userSearchRequest.getContactId();
        UserSearchVo searchVo = userContactApplyService.search(userId, contactId);
        return ResultUtils.success(searchVo);
    }

    @PostMapping("/applyAdd")
    @SaCheckLogin
    public BaseResponse<Integer> applyAdd(@RequestBody ApplyAddRequest applyAddRequest) {
        if (applyAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (StringUtils.isBlank(applyAddRequest.getContactId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        String applyInfo = applyAddRequest.getApplyInfo();
        String contactId = applyAddRequest.getContactId();
        Integer joinId = userContactApplyService.applyAdd(applyInfo, contactId);
        return ResultUtils.success(joinId);
    }

    @PostMapping("/loadApplyAdd")
    @SaCheckLogin
    public BaseResponse<Page<UserContactApply>> loadApplyAdd(@RequestBody ApplyQueryRequest applyAddRequest) {
        int pageSize = applyAddRequest.getPageSize();
        int current = applyAddRequest.getCurrent();
        String receiveUserId = applyAddRequest.getReceiveUserId();
        if (StringUtils.isBlank(receiveUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        Page<UserContactApply> applyPage = new Page<>(current,pageSize);
        userContactApplyMapper.selectUserContactApplyWithPage(applyPage,receiveUserId);
        return ResultUtils.success(applyPage);
    }

    @PostMapping("/dealWithApply")
    @SaCheckLogin
    public BaseResponse<Boolean> dealWithApply(@RequestBody ApplyDealRequest applyDealRequest) {
        if (applyDealRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }
        Integer status = applyDealRequest.getStatus();
        Integer applyId = applyDealRequest.getApplyId();
        Boolean result = userContactApplyService.dealWithApply(applyId,status);

        return ResultUtils.success(null);
    }
}
