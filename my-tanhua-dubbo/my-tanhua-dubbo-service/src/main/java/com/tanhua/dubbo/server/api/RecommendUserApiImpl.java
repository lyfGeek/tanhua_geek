package com.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class RecommendUserApiImpl implements IRecommendUserApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 今日佳人。查询一位得分最高的推荐用户。
     *
     * @param userId
     * @return
     */
    @Override
    public RecommendUser queryMaxScore(Long userId) {
        // 条件。
        Criteria criteria = Criteria.where("toUserId").is(userId);
        // 按照得分倒序排序，获取第一条数据。
        Query query = Query.query(criteria).with(Sort.by(Sort.Order.desc("score")))
                .limit(1);
        return this.mongoTemplate.findOne(query, RecommendUser.class);
    }

    /**
     * 查询推荐用户列表。按照得分倒序。
     *
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        // 条件。
        Criteria criteria = Criteria.where("toUserId").is(userId);
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Order.desc("score")));
        Query query = Query.query(criteria).with(pageable);
        List<RecommendUser> recommendUsers = this.mongoTemplate.find(query, RecommendUser.class);

        // 说明：数据总条数，暂时不提供，如果需要的话再提供。
        return new PageInfo<>(0, pageNum, pageSize, recommendUsers);
    }

    /**
     * 查询推荐好友的缘分值。
     *
     * @param userId
     * @param toUserId
     * @return
     */
    @Override
    public double queryScore(Long userId, Long toUserId) {
        Query query = Query.query(Criteria
                .where("toUserId").is(toUserId)
                .and("userId").is(userId));
        RecommendUser recommendUser = this.mongoTemplate.findOne(query, RecommendUser.class);
        if (null == recommendUser) {
            return 0;
        }

        return recommendUser.getScore();
    }

}
