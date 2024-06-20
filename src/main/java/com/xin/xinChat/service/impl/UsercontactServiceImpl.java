package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.model.entity.Usercontact;
import generator.service.UsercontactService;
import com.xin.xinChat.mapper.UsercontactMapper;
import org.springframework.stereotype.Service;

/**
* @author 15712
* @description 针对表【userContact(联系人表)】的数据库操作Service实现
* @createDate 2024-06-20 20:46:55
*/
@Service
public class UsercontactServiceImpl extends ServiceImpl<UsercontactMapper, Usercontact>
    implements UsercontactService{

}




