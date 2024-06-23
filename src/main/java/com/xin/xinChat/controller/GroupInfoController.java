package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.model.dto.group.SaveGroupRequest;
import com.xin.xinChat.service.UserService;
import generator.service.GroupinfoService;
import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/23 19:19
 */
@RestController
@RequestMapping("/group")
public class GroupInfoController {

    @Resource
    private GroupinfoService groupinfoService;

    @Resource
    private UserService userService;
    @PostMapping("/saveGroup")
    @SaCheckLogin
    public BaseResponse<Integer> saveGroup(@RequestBody SaveGroupRequest saveGroupRequest) {
        userService.getLoginUser();

        return ResultUtils.success(1);
    }

}
