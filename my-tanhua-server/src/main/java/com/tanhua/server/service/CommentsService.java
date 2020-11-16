package com.tanhua.server.service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.server.api.IQuanZiApi;
import com.tanhua.dubbo.server.pojo.Comment;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.Comments;
import com.tanhua.server.vo.PageResult;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsService {

    @Reference(version = "1.0.0")
    private IQuanZiApi quanZiApi;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 查询评论列表。
     *
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    public PageResult queryCommentsList(String publishId, Integer page, Integer pageSize) {
        User user = UserThreadLocal.get();

        PageResult pageResult = new PageResult();
        // 初始值。
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setCounts(0);
        pageResult.setPages(0);

        PageInfo<Comment> pageInfo = this.quanZiApi.queryCommentList(publishId, page, pageSize);
        List<Comment> records = pageInfo.getRecords();

        if (CollectionUtils.isEmpty(records)) {
            return pageResult;
        }

        // 用户信息。
        List<Long> userIds = new ArrayList<>();
        for (Comment record : records) {
            if (!userIds.contains(record.getUserId())) {
                userIds.add(record.getUserId());
            }
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        // 最终返回的对象。
        List<Comments> commentsList = new ArrayList<>();
        for (Comment record : records) {
            Comments comments = new Comments();

            comments.setId(record.getId().toHexString());
            comments.setCreateDate(new DateTime(record.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            comments.setContent(record.getContent());

            for (UserInfo userInfo : userInfoList) {
                if (record.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    comments.setNickname(userInfo.getNickName());
                    comments.setAvatar(userInfo.getLogo());
                    break;
                }
            }

            // 点赞数。
            String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + comments.getId();
            comments.setHasLiked(this.redisTemplate.hasKey(likeUserCommentKey) ? 1 : 0);// 是否点赞。
            String likeCommentKey = "QUANZI_COMMENT_LIKE_" + comments.getId();
            String value = this.redisTemplate.opsForValue().get(likeCommentKey);
            if (StringUtils.isNotEmpty(value)) {
                comments.setLikeCount(Integer.valueOf(value));// 点赞数。
            } else {
                comments.setLikeCount(0);// 点赞数。
            }
            commentsList.add(comments);
        }
        pageResult.setItems(commentsList);

        return pageResult;
    }

    /**
     * 保存评论。
     *
     * @param publishId
     * @param comment
     * @return
     */
    public Boolean saveComments(String publishId, String comment) {
        User user = UserThreadLocal.get();
        return this.quanZiApi.saveComment(user.getId(), publishId, 2, comment);
    }

}
