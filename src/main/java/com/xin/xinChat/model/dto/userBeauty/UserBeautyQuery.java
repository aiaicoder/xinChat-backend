package com.xin.xinChat.model.dto.userBeauty;

import com.xin.xinChat.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/11 20:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserBeautyQuery extends PageRequest {
    String email;
    String id;
}
