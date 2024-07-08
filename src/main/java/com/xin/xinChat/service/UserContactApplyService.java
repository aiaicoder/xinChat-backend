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

    /**
     * 查询添加列表
     * @param userId
     * @param contactId
     * @return
     */
    UserSearchVo search(String userId, String contactId);

    /**
     * 添加好友或者群聊
     * @param applyInfo
     * @param contactId
     * @return
     */
    Integer applyAdd(String applyInfo, String contactId);


    /**
     * 处理好友申请
     * @param applyId
     * @param status
     * @return
     */
    boolean dealWithApply(Integer applyId, Integer status);

    /**
     *
     * @param applyUserId
     * @param receiveUserId
     * @param contactId
     * @param contactType 联系人类型，群聊或者联系人
     * @param contactName
     */
     void addContact(String applyUserId,String receiveUserId, String contactId,Integer contactType,String contactName);





}
