package com.xin.xinChat.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.common.BaseResponse;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.common.ResultUtils;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.model.dto.userBeauty.UserBeautyAddRequest;
import com.xin.xinChat.model.dto.userBeauty.UserBeautyQuery;
import com.xin.xinChat.model.entity.UserBeauty;
import com.xin.xinChat.service.impl.UserBeautyServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/11 20:40
 */
@RestController
@RequestMapping("/admin/userBeauty")
public class AdminUserBeautyController {

    @Resource
    private UserBeautyServiceImpl userBeautyService;


    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userBeautyQuery
     * @return
     */
    @PostMapping("/loadBeautyList/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
        public BaseResponse<Page<UserBeauty>> loadBeautyList(@RequestBody UserBeautyQuery userBeautyQuery) {
        long current = userBeautyQuery.getCurrent();
        long size = userBeautyQuery.getPageSize();
        String email = userBeautyQuery.getEmail();
        String id = userBeautyQuery.getId();
        String sortField = userBeautyQuery.getSortField();
        String sortOrder = userBeautyQuery.getSortOrder();
        QueryWrapper<UserBeauty> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(email), "email", email);
        if (sortField != null && sortOrder != null) {
            queryWrapper.orderBy(true, "asc".equals(sortOrder), sortField);
        }
        Page<UserBeauty> userBeautyPage = userBeautyService.page(new Page<>(current, size),
                queryWrapper);
        return ResultUtils.success(userBeautyPage);
    }


    /**
     * 新增靓号
     *
     * @param userBeautyAddRequest
     * @return
     */
    @PostMapping("/addUserBeauty")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<String> addUser(@RequestBody UserBeautyAddRequest userBeautyAddRequest) {
        if (userBeautyAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //新增靓号
        UserBeauty userBeauty = new UserBeauty();
        BeanUtils.copyProperties(userBeautyAddRequest, userBeauty);
        userBeautyService.saveBeauty(userBeauty);
        return ResultUtils.success(null);
    }

    /**
     * 删除靓号，直接删库
     * @param id
     * @return
     */
    @GetMapping("/deleteUserBeauty")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<String> deleteUserBeauty(@RequestParam("id") Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userBeautyService.removeById(id);
        return ResultUtils.success(null);
    }
}
