package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.constant.CommonConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserBeautyMapper;
import com.xin.xinChat.model.dto.user.UserQueryRequest;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserBeauty;
import com.xin.xinChat.service.UserBeautyService;
import com.xin.xinChat.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 15712
* @description 针对表【UserBeauty(靓号表)】的数据库操作Service实现
* @createDate 2024-06-09 11:41:01
*/
@Service
public class UserBeautyServiceImpl extends ServiceImpl<UserBeautyMapper, UserBeauty>
    implements UserBeautyService {



}




