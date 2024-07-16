package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.mapper.ChatMessageMapper;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.service.ChatMessageService;

import org.springframework.stereotype.Service;

/**
* @author 15712
* @description 针对表【ChatMessage】的数据库操作Service实现
* @createDate 2024-07-16 20:20:50
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService {

}




