package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.dubbo.server.api.IQuanZiApi;
import com.tanhua.dubbo.server.api.IVisitorsApi;
import com.tanhua.dubbo.server.pojo.Publish;
import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.RelativeDateFormat;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.Movements;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.PicUploadResult;
import com.tanhua.server.vo.VisitorsVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class MovementsService {

    @Reference(version = "1.0.0")
    private IQuanZiApi quanZiApi;

    @Reference(version = "1.0.0")
    private IVisitorsApi visitorsApi;

    @Autowired
    private PicUploadService picUploadService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 发布动态。统一 Token。ThreadLocal 改进。
     *
     * @param textContent
     * @param location
     * @param longitude
     * @param latitude
     * @param multipartFile
     * @return
     */
    public String saveMovements(String textContent,
                                String location,
                                String longitude,
                                String latitude,
                                MultipartFile[] multipartFile) {

        User user = UserThreadLocal.get();

        Publish publish = new Publish();
        publish.setUserId(user.getId());
        publish.setText(textContent);
        publish.setLocationName(location);
        publish.setLatitude(latitude);
        publish.setLongitude(longitude);

        // 图片上传。
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            PicUploadResult uploadResult = this.picUploadService.upload(file);
            imageUrls.add(uploadResult.getName());
        }

        publish.setMedias(imageUrls);

        return this.quanZiApi.savePublish(publish);
    }

    /**
     * 发布动态。token。
     *
     * @param textContent
     * @param location
     * @param longitude
     * @param latitude
     * @param multipartFile
     * @param token
     * @return
     */
    public Boolean saveMovementsToken(String textContent,
                                      String location,
                                      String longitude,
                                      String latitude,
                                      MultipartFile[] multipartFile,
                                      String token) {
//        User user = UserThreadLocal.get();
        User user = this.userService.queryUserByToken(token);

        if (null == user) {
            return false;
        }

        Publish publish = new Publish();
        publish.setUserId(user.getId());
        publish.setText(textContent);
        publish.setLocationName(location);
        publish.setLatitude(latitude);
        publish.setLongitude(longitude);

        // 图片上传。
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            PicUploadResult uploadResult = this.picUploadService.upload(file);
            imageUrls.add(uploadResult.getName());
        }

        publish.setMedias(imageUrls);

        return this.quanZiApi.savePublishBool(publish);
    }

    private PageResult queryPublishList(User user, Integer page, Integer pageSize) {
        PageResult pageResult = new PageResult();
        Long userId = null;// 默认查询推荐动态。

        PageInfo<Publish> pageInfo = null;

        if (null == user) {
            // 查询推荐动态。
            String key = "QUANZI_PUBLISH_RECOMMEND_" + UserThreadLocal.get().getId();
            String value = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(value)) {
                // 命中了数据。
                String[] pids = StringUtils.split(value, ',');
                int startIndex = (page - 1) * pageSize;
                if (startIndex < pids.length) {
                    int endIndex = startIndex + pageSize - 1;
                    if (endIndex >= pids.length) {
                        endIndex = pids.length - 1;
                    }

                    List<Long> pidList = new ArrayList<>();
                    for (int i = startIndex; i <= endIndex; i++) {
                        pidList.add(Long.valueOf(pids[i]));
                    }

                    List<Publish> publishList = this.quanZiApi.queryPublishByPids(pidList);
                    pageInfo = new PageInfo<>();
                    pageInfo.setRecords(publishList);
                }
            }
        }

        if (pageInfo == null) {
            // 默认查询逻辑。
            if (user != null) {
                // 查询好友动态。
                userId = user.getId();
            }
            pageInfo = this.quanZiApi.queryPublishList(userId, page, pageSize);
        }

        user = UserThreadLocal.get();// 查询完成后，依然还是需要获取到当前的登录用户。

        pageResult.setCounts(0);
        pageResult.setPages(0);
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);

        List<Publish> records = pageInfo.getRecords();

        if (CollectionUtils.isEmpty(records)) {
            // 没有查询到动态数据。
            return pageResult;
        }

        pageResult.setItems(this.fillValueToMovements(records));

        return pageResult;
    }

    private List<Movements> fillValueToMovements(List<Publish> records) {
        User user = UserThreadLocal.get();
        List<Movements> movementsList = new ArrayList<>();
        List<Long> userIds = new ArrayList<>();
        for (Publish record : records) {
            Movements movements = new Movements();

            movements.setId(record.getId().toHexString());
            movements.setUserId(record.getUserId());

            if (!userIds.contains(record.getUserId())) {
                userIds.add(record.getUserId());
            }

            String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + movements.getId();
            movements.setHasLiked(this.redisTemplate.hasKey(likeUserCommentKey) ? 1 : 0);// 是否点赞。

            String likeCommentKey = "QUANZI_COMMENT_LIKE_" + movements.getId();
            String value = this.redisTemplate.opsForValue().get(likeCommentKey);
            if (StringUtils.isNotEmpty(value)) {
                movements.setLikeCount(Integer.valueOf(value));// 点赞数。
            } else {
                movements.setLikeCount(0);// 点赞数。
            }

            String loveUserCommentKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + movements.getId();
            movements.setHasLoved(this.redisTemplate.hasKey(loveUserCommentKey) ? 1 : 0);// 是否喜欢。

            String loveCommentKey = "QUANZI_COMMENT_LOVE_" + movements.getId();
            String loveValue = this.redisTemplate.opsForValue().get(loveCommentKey);
            if (StringUtils.isNotEmpty(loveValue)) {
                movements.setLoveCount(Integer.valueOf(loveValue));// 喜欢数。
            } else {
                movements.setLoveCount(0);// 喜欢数。
            }

            movements.setDistance("1.2公里");// TODO 距离。
            movements.setCommentCount(30);// TODO 评论数。
            movements.setCreateDate(RelativeDateFormat.format(new Date(record.getCreated())));// 发布时间，10分钟前。
            movements.setTextContent(record.getText());
            movements.setImageContent(record.getMedias().toArray(new String[]{}));

            movementsList.add(movements);
        }


        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(userInfoQueryWrapper);

        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfoList) {
                if (movements.getUserId().longValue() == userInfo.getUserId().longValue()) {

                    movements.setTags(StringUtils.split(userInfo.getTags(), ','));
                    movements.setNickname(userInfo.getNickName());
                    movements.setGender(userInfo.getSex().name().toLowerCase());
                    movements.setAvatar(userInfo.getLogo());
                    movements.setAge(userInfo.getAge());

                    break;
                }
            }
        }

        return movementsList;
    }

    public PageResult queryRecommendPublishList(Integer page, Integer pageSize) {
        return this.queryPublishList(null, page, pageSize);
    }

    public PageResult queryUserPublishList(Integer page, Integer pageSize) {
        return this.queryPublishList(UserThreadLocal.get(), page, pageSize);
    }

    public Long likeComment(String publishId) {
        User user = UserThreadLocal.get();

        boolean bool = this.quanZiApi.saveLikeComment(user.getId(), publishId);
        if (!bool) {
            // 保存失败。
            return null;
        }
        // 保存成功，获取点赞数。
        Long likeCount = 0L;
        String likeCommentKey = "QUANZI_COMMENT_LIKE_" + publishId;
        if (!this.redisTemplate.hasKey(likeCommentKey)) {
            Long count = this.quanZiApi.queryCommentCount(publishId, 1);
            likeCount = count;
            this.redisTemplate.opsForValue().set(likeCommentKey, String.valueOf(likeCount));
        } else {
            likeCount = this.redisTemplate.opsForValue().increment(likeCommentKey);
        }

        // 记录当前用于已经点赞。
        String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.opsForValue().set(likeUserCommentKey, "1");

        return likeCount;
    }

    public Long disLikeComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean bool = this.quanZiApi.removeComment(user.getId(), publishId, 1);
        if (!bool) {
            return null;
        }

        // redis 中的点赞数需要减少 1。
        String likeCommentKey = "QUANZI_COMMENT_LIKE_" + publishId;
        Long count = this.redisTemplate.opsForValue().decrement(likeCommentKey);

        // 删除该用户的标记点赞。
        String likeUserCommentKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.delete(likeUserCommentKey);

        return count;
    }

    public Long loveComment(String publishId) {
        User user = UserThreadLocal.get();

        boolean bool = this.quanZiApi.saveLoveComment(user.getId(), publishId);
        if (!bool) {
            // 保存失败。
            return null;
        }

        // 保存成功，获取喜欢数。
        Long loveCount = 0L;
        String likeCommentKey = "QUANZI_COMMENT_LOVE_" + publishId;
        if (!this.redisTemplate.hasKey(likeCommentKey)) {
            Long count = this.quanZiApi.queryCommentCount(publishId, 1);
            loveCount = count;
            this.redisTemplate.opsForValue().set(likeCommentKey, String.valueOf(loveCount));
        } else {
            loveCount = this.redisTemplate.opsForValue().increment(likeCommentKey);
        }

        // 记录当前用于已经喜欢。
        String likeUserCommentKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.opsForValue().set(likeUserCommentKey, "1");

        return loveCount;
    }

    public Long unLoveComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean bool = this.quanZiApi.removeComment(user.getId(), publishId, 3);
        if (!bool) {
            return null;
        }

        // redis 中的喜欢数需要减少 1。
        String likeCommentKey = "QUANZI_COMMENT_LOVE_" + publishId;
        Long count = this.redisTemplate.opsForValue().decrement(likeCommentKey);

        // 删除该用户的标记喜欢。
        String likeUserCommentKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.delete(likeUserCommentKey);

        return count;
    }

    public Movements queryMovementsById(String publishId) {
        Publish publish = this.quanZiApi.queryPublishById(publishId);
        if (null == publish) {
            return null;
        }

        // 查询到动态数据，数据的填充。
        List<Movements> movementsList = this.fillValueToMovements(Arrays.asList(publish));
        return movementsList.get(0);
    }

    public List<VisitorsVo> queryVisitorsList() {
        User user = UserThreadLocal.get();

        // 如果 redis 中存在上一次查询的时间点的话就按照时间查询，否则就按照数量查询。
        String redisKey = "visitors_" + user.getId();
        String data = this.redisTemplate.opsForValue().get(redisKey);
        List<Visitors> visitors = null;
        if (StringUtils.isEmpty(data)) {
            // 按照数量查询。
            visitors = this.visitorsApi.topVisitor(user.getId(), 6);
        } else {
            // 按照时间查询。
            visitors = this.visitorsApi.topVisitor(user.getId(), Long.valueOf(data));
        }

        if (CollectionUtils.isEmpty(visitors)) {
            return Collections.emptyList();
        }

        List<Long> userIds = new ArrayList<>();
        for (Visitors visitor : visitors) {
            userIds.add(visitor.getVisitorUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<VisitorsVo> result = new ArrayList<>();

        for (Visitors visitor : visitors) {
            for (UserInfo userInfo : userInfoList) {
                if (visitor.getVisitorUserId().longValue() == userInfo.getUserId().longValue()) {
                    VisitorsVo visitorsVo = new VisitorsVo();

                    visitorsVo.setAge(userInfo.getAge());
                    visitorsVo.setAvatar(userInfo.getLogo());
                    visitorsVo.setGender(userInfo.getSex().name().toLowerCase());
                    visitorsVo.setId(userInfo.getUserId());
                    visitorsVo.setNickname(userInfo.getNickName());
                    visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
                    visitorsVo.setFateValue(visitor.getScore().intValue());

                    result.add(visitorsVo);
                    break;
                }
            }
        }

        return result;
    }

    public PageResult queryAlbumList(Long userId, Integer page, Integer pageSize) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);

        PageInfo<Publish> albumPageInfo = this.quanZiApi.queryAlbumList(userId, page, pageSize);
        List<Publish> records = albumPageInfo.getRecords();

        if (CollectionUtils.isEmpty(records)) {
            return pageResult;
        }
        pageResult.setItems(this.fillValueToMovements(records));
        return pageResult;
    }

}
