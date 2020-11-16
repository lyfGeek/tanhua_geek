package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;

public interface IRecommendUserApi {

    /**
     * 今日佳人。查询一位得分最高的推荐用户。
     *
     * @param userId
     * @return
     */
    RecommendUser queryMaxScore(Long userId);

    /**
     * 查询推荐用户列表。按照得分倒序。
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 查询推荐好友的缘分值。
     *
     * @param userId
     * @param toUserId
     * @return
     */
    double queryScore(Long userId, Long toUserId);

}
