package com.xin.xinChat.utils;

import cn.hutool.json.JSONUtil;
import com.xin.xinChat.constant.RedisKeyConstant;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/25 19:51
 */
@Component
public class SysSettingUtil {
    @Resource
    private  RedisUtils redisUtils;


    public SysSettingDTO getSysSetting() {
        SysSettingDTO sysSettingDTO;
        String sysSettingStr = redisUtils.get(RedisKeyConstant.REDIS_KEY_SYS_SETTING);
        if (StringUtils.isBlank(sysSettingStr)) {
            sysSettingDTO = new SysSettingDTO();
        } else {
            sysSettingDTO = JSONUtil.toBean(sysSettingStr, SysSettingDTO.class);
        }
        return sysSettingDTO;
    }

}
