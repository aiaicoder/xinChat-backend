package com.xin.springbootinit.model.vo;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.xin.springbootinit.model.entity.Comment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 15712
 */
@Data
public class CommentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long Id;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String content;

    private Long parentId;

    private Integer commentNum;

    private Integer likeCount;

    private Boolean isLike;

    private List<String> likeListId;

    private Boolean inputShow;

    private Long fromId;

    private String fromName;

    private Date gmtModified;

    private Date gmtCreate;

    private List<CommentVO> reply;

    /**
     * 包装类转对象
     *
     * @param CommentVO
     * @return
     */
    public static Comment voToObj(CommentVO CommentVO) {
        if (CommentVO == null) {
            return null;
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(CommentVO, comment);
        List<String> likeListId = CommentVO.getLikeListId();
        if (likeListId != null) {
            String likeListIdStr = JSONUtil.toJsonStr(likeListId);
            comment.setLikeListId(likeListIdStr);
        }

        return comment;
    }

    /**
     * 对象转包装类
     *
     * @param Comment
     * @return
     */
    public static CommentVO objToVo(Comment Comment) {
        if (Comment == null) {
            return null;
        }
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(Comment, commentVO);
        String judgeInfoStr = Comment.getLikeListId();
        if (StrUtil.isNotBlank(judgeInfoStr)) {
            commentVO.setLikeListId(JSONUtil.toList(judgeInfoStr, String.class));
        }
        return commentVO;
    }
}

