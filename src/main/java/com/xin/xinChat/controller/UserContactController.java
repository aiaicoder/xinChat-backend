package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.model.dto.search.UserSearchRequest;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.UserContactService;
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
        UserSearchVo searchVo = userContactService.search(userId, contactId);
        return ResultUtils.success(searchVo);
    }
}
