package com.xin.xinChat.manager;

import com.xin.xinChat.config.AppConfig;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.xin.xinChat.constant.RedisKeyConstant.REDIS_AI_KEY;


/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/8/1 19:59
 */
@Service
@Slf4j
public class AiManager {

    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private AppConfig appConfig;

    public String doChat(String uid,String message) {
        boolean b = redisLimiterManager.doRateLimitAI(REDIS_AI_KEY + uid);
        if (!b){
            return "亲爱的,你今天找我聊了" + appConfig.getAiLimit() + "次了~人家累了~明天见";
        }
        try {
            DevChatRequest devChatRequest = new DevChatRequest();
            devChatRequest.setModelId(1818981164685131777L);
            devChatRequest.setMessage(message);
            BaseResponse<DevChatResponse> devChatResponseBaseResponse = yuCongMingClient.doChat(devChatRequest);
            String content = devChatResponseBaseResponse.getData().getContent();
            if (content == null){
                return "我生病了，等我康复了在聊吧";
            }
            return content;
        }catch (Exception e) {
            log.error("AI 错误", e);
            return "我生病了，等我康复了在聊吧";
        }
    }
}
