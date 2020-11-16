package com.tanhua.dubbo.server.api;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.tanhua.dubbo.server.pojo.*;
import com.tanhua.dubbo.server.service.IdService;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@Service(version = "1.0.0")// com.alibaba.dubbo.config.annotation.Service;
public class QuanziApiImpl implements IQuanZiApi {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private IdService idService;

    @Override
    public boolean savePublishBool(Publish publish) {
        // 校验 publish 对象。
        if (publish.getUserId() == null) {
            return false;
        }
        // 其他的逻辑类似。

        try {
            // 填充数据。
            publish.setId(ObjectId.get());
            publish.setCreated(System.currentTimeMillis());// 发布时间。
            publish.setSeeType(1);// 查看权限。

            // 增加自增长的 pid。
            publish.setPid(this.idService.createId("publish", publish.getId().toHexString()));

            // 保存动态信息。
            this.mongoTemplate.save(publish);

            // 写入到自己的相册表中。
            Album album = new Album();
            album.setId(ObjectId.get());
            album.setPublishId(publish.getId());// 动态 id。
            album.setCreated(System.currentTimeMillis());

            // 将相册对象写入到 MongoDB 中。每个人都有自己的相册表。
            this.mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());

            // 查询当前用户的好友数据，将动态数据写入到好友的时间线表中。
            Query query = Query.query(Criteria.where("userId").is(publish.getUserId()));
            List<Users> friends = this.mongoTemplate.find(query, Users.class);
            for (Users user : friends) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setUserId(publish.getUserId());// 我。（发表人的 id。）
                timeLine.setPublishId(publish.getId());
                timeLine.setDate(System.currentTimeMillis());

                // 写入好友的时间线表。
                this.mongoTemplate.save(timeLine, "quanzi_time_line_" + user.getFriendId());
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // todo 事务回滚。
        }

        return false;
    }

    /**
     * 发布动态。
     *
     * @param publish
     * @return 主键 id。
     */
    @Override
    public String savePublish(Publish publish) {
        // 校验 publish 对象。
        if (publish.getUserId() == null) {
            return null;
        }
        // 其他的逻辑类似。

        try {
            // 填充数据。
            publish.setId(ObjectId.get());
            publish.setCreated(System.currentTimeMillis());// 发布时间。
            publish.setSeeType(1);// 查看权限。

            // 增加自增长的 pid。
            publish.setPid(this.idService.createId("publish", publish.getId().toHexString()));

            // 保存动态信息。
            this.mongoTemplate.save(publish);

            // 写入到自己的相册表中。
            Album album = new Album();
            album.setId(ObjectId.get());
            album.setPublishId(publish.getId());// 动态 id。
            album.setCreated(System.currentTimeMillis());

            // 将相册对象写入到 MongoDB 中。
            this.mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());

            // 查询当前用户的好友数据，将动态数据写入到好友的时间线表中。
            Query query = Query.query(Criteria.where("userId").is(publish.getUserId()));
            List<Users> users = this.mongoTemplate.find(query, Users.class);
            for (Users user : users) {
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setUserId(publish.getUserId());
                timeLine.setPublishId(publish.getId());
                timeLine.setDate(System.currentTimeMillis());

                this.mongoTemplate.save(timeLine, "quanzi_time_line_" + user.getFriendId());
            }

            return publish.getId().toHexString();
        } catch (Exception e) {
            e.printStackTrace();
            // TODO 事务回滚。
        }

        return null;
    }

    /**
     * 查询好友动态。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("date")));
        Query query = new Query().with(pageable);
        String tableName = "quanzi_time_line_";
        if (null == userId) {
            // 查询推荐动态。
            tableName += "recommend";
        } else {
            // 查询好友动态。
            tableName += userId;
        }

        // 查询自己的时间线表。
        List<TimeLine> timeLines = this.mongoTemplate.find(query, TimeLine.class, tableName);

        List<ObjectId> ids = new ArrayList<>();
        for (TimeLine timeLine : timeLines) {
            ids.add(timeLine.getPublishId());
        }

        Query queryPublish = Query.query(Criteria.where("id").in(ids)).with(Sort.by(Sort.Order.desc("created")));
        // 查询动态信息。
        List<Publish> publishList = this.mongoTemplate.find(queryPublish, Publish.class);

        // 封装分页对象。
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);// 不提供总数。
        pageInfo.setRecords(publishList);

        return pageInfo;
    }

    /**
     * 点赞。
     *
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public boolean saveLikeComment(Long userId, String publishId) {
        //判断是否已经点赞，如果已经点赞就返回。
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(1);
        Query query = Query.query(criteria);
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            return false;
        }

        return this.saveComment(userId, publishId, 1, null);
    }

    /**
     * 取消点赞、喜欢等。
     *
     * @param userId
     * @param publishId
     * @param commentType
     * @return
     */
    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(commentType);
        Query query = Query.query(criteria);

        DeleteResult deleteResult = this.mongoTemplate.remove(query, Comment.class);
        return deleteResult.getDeletedCount() > 0;
    }

    /**
     * 喜欢。
     *
     * @param userId
     * @param publishId
     * @return
     */
    @Override
    public boolean saveLoveComment(Long userId, String publishId) {
        // 判断是否已经喜欢，如果已经喜欢就返回。
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("publishId").is(new ObjectId(publishId))
                .and("commentType").is(3);
        Query query = Query.query(criteria);
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            return false;
        }

        return this.saveComment(userId, publishId, 3, null);
    }

    /**
     * 保存发表评论。
     *
     * @param userId
     * @param publishId
     * @param type
     * @param content
     * @return
     */
    @Override
    public boolean saveComment(Long userId, String publishId, Integer type, String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setIsParent(true);
            comment.setCommentType(type);
            comment.setPublishId(new ObjectId(publishId));
            comment.setUserId(userId);
            comment.setId(ObjectId.get());
            comment.setCreated(System.currentTimeMillis());

            // 设置发布人的 id。
            Publish publish = this.mongoTemplate.findById(comment.getPublishId(), Publish.class);
            if (null != publish) {
                comment.setPublishUserId(publish.getUserId());
            } else {
                Video video = this.mongoTemplate.findById(comment.getPublishId(), Video.class);
                if (null != video) {
                    comment.setPublishUserId(video.getUserId());
                }
            }
            this.mongoTemplate.save(comment);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 查询评论数。
     *
     * @param publishId
     * @param type
     * @return
     */
    @Override
    public Long queryCommentCount(String publishId, Integer type) {
        Criteria criteria = Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(type);
        Query query = Query.query(criteria);
        return this.mongoTemplate.count(query, Comment.class);
    }

    /**
     * 根据 id 查询。
     *
     * @param publishId
     * @return
     */
    @Override
    public Publish queryPublishById(String publishId) {
        return this.mongoTemplate.findById(new ObjectId(publishId), Publish.class);
    }

    /**
     * 查询评论。
     *
     * @param publishId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.asc("created")));
        Query query = Query.query(Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(2)).with(pageRequest);
        List<Comment> commentList = this.mongoTemplate.find(query, Comment.class);
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setTotal(0);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(page);
        pageInfo.setRecords(commentList);
        return pageInfo;
    }

    /**
     * 根据 pid 批量查询数据。
     *
     * @param pids
     * @return
     */
    @Override
    public List<Publish> queryPublishByPids(List<Long> pids) {
        Query query = Query.query(Criteria.where("pid").in(pids));
        return this.mongoTemplate.find(query, Publish.class);
    }

    /**
     * 查询用户的评论数据。
     *
     * @param userId
     * @param type
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Comment> queryCommentListByUser(Long userId, Integer type, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("created")));
        Query query = new Query(Criteria
                .where("publishUserId").is(userId)
                .and("commentType").is(type)).with(pageRequest);

        List<Comment> commentList = this.mongoTemplate.find(query, Comment.class);

        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(commentList);
        pageInfo.setTotal(0);// 不提供总数。
        return pageInfo;
    }

    /**
     * 查询相册表。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageInfo<Publish> queryAlbumList(Long userId, Integer page, Integer pageSize) {
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0);// 不提供总数。

        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("created")));
        Query query = new Query().with(pageable);
        List<Album> albumList = this.mongoTemplate.find(query, Album.class, "quanzi_album_" + userId);

        if (CollectionUtils.isEmpty(albumList)) {
            return pageInfo;
        }

        // 查询相册所对应的动态信息。
        List<ObjectId> publishIds = new ArrayList<>();
        for (Album album : albumList) {
            publishIds.add(album.getPublishId());
        }

        Query publishQuery = Query.query(Criteria.where("id").in(publishIds)).with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = this.mongoTemplate.find(publishQuery, Publish.class);

        pageInfo.setRecords(publishList);
        return pageInfo;
    }

}
