package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.utils.SysSettingUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/11 19:42
 */
@RestController()
@RequestMapping("/admin/setting")
public class AdminSettingController {


    @Resource
    private SysSettingUtil sysSettingUtil;


    @GetMapping("/getSysSetting")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<SysSettingDTO> getSysSetting() {
        SysSettingDTO sysSettingDTO = sysSettingUtil.getSysSetting();
        return ResultUtils.success(sysSettingDTO);
    }

    @PostMapping("/saveSysSetting")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateSysSetting(@RequestBody SysSettingDTO sysSettingDTO) {
        sysSettingUtil.saveSysSetting(sysSettingDTO);
        return ResultUtils.success(true);
    }


}
