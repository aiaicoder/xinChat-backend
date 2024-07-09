package com.xin.xinChat.model.dto.contact;

import com.xin.xinChat.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/9 20:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoadUserContactRequest extends PageRequest {
    private String userId;
    private String contactType;
}
