package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Comment;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface IQuanZiApi {

    /**
     * 发布动态。
     *
     * @param publish
     * @return 主键 id。
     */
    String savePublish(Publish publish);

    boolean savePublishBool(Publish publish);

    /**
     * 查询好友动态 + 推荐动态。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize);

    /**
     * 点赞动态。
     *
     * @param userId
     * @param publishId
     * @return
     */
    boolean saveLikeComment(Long userId, String publishId);

    /**
     * 取消点赞、喜欢等评论。
     *
     * @param userId
     * @param publishId
     * @param commentType 评论类型，1 ~ 点赞，2 ~ 评论，3 ~ 喜欢。
     * @return
     */
    boolean removeComment(Long userId, String publishId, Integer commentType);

    /**
     * 喜欢评论。
     *
     * @param userId
     * @param publishId
     * @return
     */
    boolean saveLoveComment(Long userId, String publishId);

    /**
     * 保存评论。
     *
     * @param userId
     * @param publishId
     * @param type
     * @param content
     * @return
     */
    boolean saveComment(Long userId, String publishId, Integer type, String content);

    /**
     * 查询评论数。
     *
     * @param publishId
     * @param type
     * @return
     */
    Long queryCommentCount(String publishId, Integer type);

    /**
     * 根据 id 查询动态。
     *
     * @param publishId
     * @return
     */
    Publish queryPublishById(String publishId);

    /**
     * 查询评论列表。
     *
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize);

    /**
     * 根据 pid 批量查询动态。
     *
     * @param pids
     * @return
     */
    List<Publish> queryPublishByPids(List<Long> pids);

    /**
     * 查询用户的评论数据。
     *
     * @param userId
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Comment> queryCommentListByUser(Long userId, Integer type, Integer page, Integer pageSize);

    /**
     * 查询相册列表。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Publish> queryAlbumList(Long userId, Integer page, Integer pageSize);

}
