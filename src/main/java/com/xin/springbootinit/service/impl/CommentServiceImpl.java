package com.xin.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.springbootinit.common.ErrorCode;
import com.xin.springbootinit.exception.BusinessException;
import com.xin.springbootinit.mapper.CommentMapper;
import com.xin.springbootinit.model.entity.Comment;
import com.xin.springbootinit.model.entity.User;
import com.xin.springbootinit.model.vo.CommentVO;
import com.xin.springbootinit.service.CommentService;
import com.xin.springbootinit.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 帖子收藏服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private CommentMapper commentMapper;


    @Override
    public List<CommentVO> getAllCommentList(long Id) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("Id", Id);
        queryWrapper.eq("parentId", -1);
        List<Comment> commentList = this.list(queryWrapper);
        List<CommentVO> commentVos = new ArrayList<>();
        commentList.forEach(comment -> {
            CommentVO commentVO = CommentVO.objToVo(comment);
            commentVos.add(commentVO);
        });
        commentVos.forEach(commentVo -> {
            //查询子评论
            QueryWrapper<Comment> childCommentQueryWrapper = new QueryWrapper<>();
            childCommentQueryWrapper.eq("parentId", commentVo.getId());
            List<Comment> childCommentList = this.list(childCommentQueryWrapper);
            List<CommentVO> childCommentVos = new ArrayList<>();
            childCommentList.forEach(childComment -> {
                CommentVO childCommentVO = CommentVO.objToVo(childComment);
                childCommentVos.add(childCommentVO);
            });
            commentVo.setReply(childCommentVos);
        });
        return commentVos;
    }

    @Override
    @Transactional
    public int deleteCommentById(Comment Comment, User loginUser) {
        //只有自己的评论和管理员才可以删除
        if (!Comment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "你无权删除该评论");
        }
        int deleteCount = 1;
        try {
            //先查询该评论是不是顶级评论，有可能有回复有可能没有回复
            QueryWrapper<Comment> isParentWrapper = new QueryWrapper<>();
            isParentWrapper.eq("id", Comment.getId());
            isParentWrapper.eq("parentId", -1);

            long count = this.count(isParentWrapper);
            // 如果count大于0说明该评论是一条顶级评论，先删除他的子级评论
            if (count > 0) {
                //先删除子评论
                QueryWrapper<Comment> wrapper = new QueryWrapper<>();
                wrapper.eq("parentId", Comment.getId());
                //删除子评论
                commentMapper.delete(wrapper);
            }
            //最后删除父级评论
            QueryWrapper<Comment> wrapper = new QueryWrapper<>();
            wrapper.eq("userId", Comment.getUserId());
            wrapper.eq("id", Comment.getId());
            commentMapper.delete(wrapper);

            // 找到该记录的顶级评论让他的评论数-1,因为有可能他的还有父级
            Long parentId = Comment.getParentId();
            Long fromId = Comment.getFromId();
            if (parentId != null && parentId.equals(fromId)) {
                Comment parentComment = this.getById(parentId);
                if (parentComment != null) {
                    parentComment.setCommentNum(parentComment.getCommentNum() - 1);
                    this.updateLikeCount(parentComment);
                }
            }

            // 考虑到不是顶级记录的直接子记录的情况 fromId:表示该记录回复的是那一条记录
            if (parentId != null && parentId.equals(fromId)) {
                // 更新他的直接父级
                Comment father = this.getById(fromId);
                if (father != null) {
                    father.setCommentNum(father.getCommentNum() - 1);
                    this.updateLikeCount(father);
                }
                // 更新他的跟节点评论数量
                Comment root = this.getById(parentId);
                if (root != null) {
                    root.setCommentNum(root.getCommentNum() - 1);
                    this.updateLikeCount(root);
                }
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除评论失败");
        }
        return deleteCount;
    }

    @Override
    public boolean addComment(Comment current, Comment parent, User loginUser) {
        Long userId = loginUser.getId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (current == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        }
        Long parentId = current.getParentId();
        // 是一条顶级评论，直接进行添加操作 如果他的parentId=-1那就是顶级评论[发表评论]
        if (current.getId() != null && parentId == -1) {
            // 如果从token中解析出来的memberId等于提交数据中MemberId就评论成功，否则失败
            if (userId.equals(current.getUserId())) {
                return this.save(current);
            }
        }
        // 如果能直接到这里，说明是一条回复评论
        if (parent != null && (parent.getId() != null && parent.getParentId() != null)) {
            // 修改当前被回复的记录的总回复条数+1 [前端传递过来的时候已经+1，直接按照id修改即可]
            this.updateLikeCount(parent);
            // 根据parentId查询一条记录
            Comment root = this.getById(parent.getParentId());
            if (root != null && root.getParentId() == -1) {
                // 根据当前被回复的记录找到顶级记录，将顶级记录也+1
                root.setCommentNum(root.getCommentNum() + 1);
                this.updateLikeCount(root);
            }
        }
        // 如果userId不等于提交数据中的userId就评论失败，否则成功
        if (userId.equals(current.getUserId())) {
            return this.save(current);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论失败");
        }
    }

    @Override
    public boolean updateLikeCount(Comment Comment) {
        return this.updateById(Comment);
    }
}




