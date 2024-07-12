package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.PageRequest;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.exception.ThrowUtils;
import com.xin.xinChat.mapper.GroupInfoMapper;
import com.xin.xinChat.model.dto.group.GroupInfoQueryRequest;
import com.xin.xinChat.model.dto.userBeauty.UserBeautyAddRequest;
import com.xin.xinChat.model.dto.userBeauty.UserBeautyQuery;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserBeauty;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.impl.UserBeautyServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.SQLSyntaxErrorException;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/11 20:40
 */
@RestController
@RequestMapping("/admin/groupInfo")
public class AdminGroupInfoController {

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private GroupInfoService groupInfoService;


    /**
     * 分页获取群组列表（仅管理员）
     *
     * @return
     */
    @PostMapping("/loadGroup/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
        public BaseResponse<Page<GroupInfo>> loadGroupInfo(@RequestBody GroupInfoQueryRequest groupInfoQueryRequest) {
        PageRequest pageRequest = new PageRequest();
        int pageSize = pageRequest.getPageSize();
        int current = pageRequest.getCurrent();
        Page<GroupInfo> groupInfoPage = new Page<>(current,pageSize);
        groupInfoMapper.loadGroupInfo(groupInfoPage,groupInfoQueryRequest);
        return ResultUtils.success(groupInfoPage);
    }





    /**
     * 分页获取群组列表（仅管理员）
     *
     * @return
     */
    @GetMapping("/dismissGroup/groupId")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<GroupInfo>> dismissGroup(@NotNull String groupId) {
        GroupInfo groupInfo = groupInfoService.getById(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        groupInfoService.dismissGroup(groupInfo.getGroupOwnerId(),groupId);
        return ResultUtils.success(null);
    }

}
