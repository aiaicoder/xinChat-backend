package com.xin.xinChat.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 * @author <a href="https://github.com/liyupi">小新</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class DeleteRequest implements Serializable {


    /**
     * id
     */
    private String id;

    /**
     * email
     */
    private String email;

    private static final long serialVersionUID = 1L;
}