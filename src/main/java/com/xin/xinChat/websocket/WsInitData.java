package com.xin.xinChat.websocket;

import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.ChatSessionUser;
import lombok.Data;

import java.util.List;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/16 21:02
 */
@Data
public class WsInitData {
    /**
     * 会话列表
     */
    private List<ChatSessionUser> chatSessionUserList;

    /**
     * 消息列表
     */
    private List<ChatMessage> chatMessageList;

    /**
     * 申请数量
     */
    private Integer applyCount;



}
