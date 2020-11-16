package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;

public interface IUserLikeApi {

    /**
     * 保存喜欢。
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    String saveUserLike(Long userId, Long likeUserId);

    /**
     * 是否相互喜欢。
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean isMutualLike(Long userId, Long likeUserId);

    /**
     * 取消用户喜欢。
     *
     * @param userId
     * @param likeUserId
     * @return
     */
    Boolean deleteUserLike(Long userId, Long likeUserId);

    /**
     * 查询相互喜欢的数量。
     *
     * @param userId
     * @return
     */
    Long queryEachLikeCount(Long userId);

    /**
     * 查询喜欢数。
     *
     * @param userId
     * @return
     */
    Long queryLikeCount(Long userId);

    /**
     * 查询粉丝数。
     *
     * @param userId
     * @return
     */
    Long queryFanCount(Long userId);

    /**
     * 查询相互喜欢列表。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryEachLikeList(Long userId, Integer page, Integer pageSize);

    /**
     * 查询我喜欢的列表。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryLikeList(Long userId, Integer page, Integer pageSize);

    /**
     * 查询粉丝列表。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<UserLike> queryFanList(Long userId, Integer page, Integer pageSize);

}
