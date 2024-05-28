package com.xin.springbootinit.model.dto.comment;


import com.xin.springbootinit.model.entity.Comment;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 15712
 */
@Data
public class CommentDeleteRequest implements Serializable {

    /**
     * 当前评论
     */
    private Comment currentComment;


}
