package com.xin.xinChat.service;

import com.xin.xinChat.model.entity.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinChat.model.vo.UserSearchVo;

/**
* @author 15712
* @description 针对表【userContactApply(联系人申请)】的数据库操作Service
* @createDate 2024-06-20 20:47:02
*/
public interface UserContactApplyService extends IService<UserContactApply> {

    UserSearchVo search(String userId, String contactId);

    Integer applyAdd(String applyInfo, String contactId);

    boolean dealWithApply(Integer applyId, Integer status);

}
