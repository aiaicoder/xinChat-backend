package com.xin.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.xin.springbootinit.model.entity.Comment;
import com.xin.springbootinit.model.entity.User;
import com.xin.springbootinit.model.vo.CommentVO;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

/**
 * @author 15712
 */
public interface CommentService extends IService<Comment> {

    /**
     * 根据问题id获取到所有的评论列表
     * @param Id 问题id
     * @return
     */
    public List<CommentVO> getAllCommentList(@Param("Id") long Id);

    /**
     * 根据评论id删除一条记录[是本人评论的记录]
     * @param comment 评论对象中包含回复人的id也包含被回复人的id
     * @return
     */
    public int deleteCommentById(Comment comment, User loginUser);

    /**
     * 添加一条评论或回复记录
     * @param current 当前提交的新comment对象
     * @param  parent  当前被点击回复的对象[评论时不需要，回复需要根据他进行判断]
     * @return
     */
    boolean addComment(Comment current,Comment parent,User loginUser);


    /**
     * 修改点赞数量
     * @param Comment 评论对象
     * @return
     */
    boolean updateLikeCount(Comment Comment);

}
