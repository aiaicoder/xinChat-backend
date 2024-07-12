package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserBeautyMapper;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserBeauty;
import com.xin.xinChat.model.enums.BeautyAccountStatusEnum;
import com.xin.xinChat.service.UserBeautyService;
import com.xin.xinChat.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 15712
 * @description 针对表【UserBeauty(靓号表)】的数据库操作Service实现
 * @createDate 2024-06-09 11:41:01
 */
@Service
public class UserBeautyServiceImpl extends ServiceImpl<UserBeautyMapper, UserBeauty>
        implements UserBeautyService {

    @Resource
    @Lazy
    private UserService userService;

    /**
     * 包括新增和修改
     *
     * @param userBeauty
     */
    @Override
    public void saveBeauty(UserBeauty userBeauty) {
        //修改操作
        if (userBeauty.getId() != null) {
            UserBeauty dbInfo = getById(userBeauty.getId());
            if (BeautyAccountStatusEnum.USING.getStatus().equals(dbInfo.getStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号正在使用中");
            }
        }
        //新增
        QueryWrapper<UserBeauty> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", userBeauty.getEmail());
        UserBeauty dbInfo = getOne(queryWrapper);
        //新增的时候判断邮箱是否存在
        if (userBeauty.getId() == null && dbInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已存在");
        }

        //修改的时候判断邮箱是否存在
        if (userBeauty.getId() != null && dbInfo != null && !userBeauty.getId().equals(dbInfo.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已存在");
        }

        //判断靓号是否存在
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userBeauty.getUserid());
        dbInfo = getOne(queryWrapper);
        //新增的时候判断靓号是否存在
        if (userBeauty.getId() == null && dbInfo != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号已存在");
        }

        //修改的时候判断靓号是否存在
        if (userBeauty.getId() != null && dbInfo != null && !userBeauty.getUserid().equals(dbInfo.getUserid())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号已存在");
        }

        //判断邮箱是否注册
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("email", userBeauty.getEmail());
        if (userService.getOne(userQueryWrapper) != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号邮箱已被注册");
        }
        User user = userService.getById(userBeauty.getUserid());
        if (user != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "靓号已被注册");
        }

        //新增
        if (userBeauty.getId() != null) {
            updateById(userBeauty);
        } else {
            //修改
            save(userBeauty);
        }


    }
}




