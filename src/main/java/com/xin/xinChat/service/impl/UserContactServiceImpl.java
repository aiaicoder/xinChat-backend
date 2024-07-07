package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.UserContactMapper;
import com.xin.xinChat.model.entity.GroupInfo;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.entity.UserContactApply;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.GroupInfoService;
import com.xin.xinChat.service.UserContactApplyService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 15712
 * @description 针对表【userContact(联系人表)】的数据库操作Service实现
 * @createDate 2024-06-20 20:46:55
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact>
        implements UserContactService {




}




