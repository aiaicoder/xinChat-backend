package com.xin.xinChat.model.dto.apply;

import com.xin.xinChat.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/4 20:12
 */

@Data
public class ApplyQueryRequest extends PageRequest {
    public String receiveUserId;

}
