package com.xin.xinChat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.model.entity.UserContact;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 15712
 * @description 针对表【userContact(联系人表)】的数据库操作Mapper
 * @createDate 2024-06-20 20:46:55
 * @Entity com.xin.xinChat.model.entity.UserContact
 */
public interface UserContactMapper extends BaseMapper<UserContact> {

    IPage<UserContact> selectUserFriend(Page<UserContact> page, @Param("userId") String userId,
                                        @Param("contactType") Integer contactType,
                                        @Param("status") Integer[] status);


    IPage<UserContact> selectMyJoinGroup(Page<UserContact> page,
                                         @Param("userId") String userId,
                                         @Param("contactType") Integer contactType,
                                         @Param("status") Integer[] status
    );



}




