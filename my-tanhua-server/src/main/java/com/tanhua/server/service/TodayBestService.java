package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanhua.dubbo.server.api.IUserLikeApi;
import com.tanhua.dubbo.server.api.IUserLocationApi;
import com.tanhua.dubbo.server.pojo.RecommendUser;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.dubbo.server.vo.UserLocationVo;
import com.tanhua.server.enums.SexEnum;
import com.tanhua.server.pojo.Question;
import com.tanhua.server.pojo.User;
import com.tanhua.server.pojo.UserInfo;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.NearUserVo;
import com.tanhua.server.vo.PageResult;
import com.tanhua.server.vo.RecommendUserQueryParam;
import com.tanhua.server.vo.TodayBest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class TodayBestService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private UserService userService;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RecommendUserService recommendUserService;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${tanhua.sso.default.user}")
    private Long defaultUserId;
    @Value("${tanhua.sso.url}")
    private String ssoUrl;
    @Value("${tanhua.sso.default.recommend.users}")
    private String defaultRecommendUsers;

    @Reference(version = "1.0.0")
    private IUserLikeApi userLikeApi;

    @Reference(version = "2.0.0")// 2.0.0 是 ES 版本的实现，1.0.0 是 MongoDB 版本的实现。
    private IUserLocationApi userLocationApi;

    @Autowired
    private IMService imService;

    /**
     * v1。
     *
     * @param token
     * @return
     */
    public TodayBest queryTodayBestToken(String token) {
        // 根据 token 查询当前登录的用户信息。
        User user = this.userService.queryUserByToken(token);
//        User user = UserThreadLocal.get();

        TodayBest todayBest = this.recommendUserService.queryTodayBest(user.getId());
        if (todayBest == null) {
            // 未找到最高得分的推荐用户，给出一个默认推荐用户。
            todayBest = new TodayBest();
            todayBest.setId(defaultUserId);
            todayBest.setFateValue(95L);
        }

        // 补全用户信息。
        UserInfo userInfo = this.userInfoService.queryUserInfoById(todayBest.getId());
        if (null != userInfo) {
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(null == userInfo.getSex() ? null : userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        }

        return todayBest;
    }

    /**
     * ThreadLocal。
     *
     * @param token
     * @return
     */
    public TodayBest queryTodayBest() {
        // 根据 token 查询当前登录的用户信息。
//        User user = this.userService.queryUserByToken(token);
        User user = UserThreadLocal.get();

        TodayBest todayBest = this.recommendUserService.queryTodayBest(user.getId());
        if (todayBest == null) {
            // 未找到最高得分的推荐用户，给出一个默认推荐用户。
            todayBest = new TodayBest();
            todayBest.setId(defaultUserId);
            todayBest.setFateValue(95L);
        }

        // 补全用户信息。
        UserInfo userInfo = this.userInfoService.queryUserInfoById(todayBest.getId());
        if (null != userInfo) {
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(null == userInfo.getSex() ? null : userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        }

        return todayBest;
    }

    public TodayBest queryTodayBest(Long userId) {
        User user = UserThreadLocal.get();

        TodayBest todayBest = new TodayBest();
        // 补全信息。
        UserInfo userInfo = this.userInfoService.queryUserInfoById(userId);
        todayBest.setId(userId);
        todayBest.setAge(userInfo.getAge());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setGender(userInfo.getSex().name().toLowerCase());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

        double score = this.recommendUserService.queryScore(userId, user.getId());
        if (score == 0) {
            score = 98;// 默认分值。
        }

        todayBest.setFateValue(Double.valueOf(score).longValue());
        return todayBest;
    }

    /**
     * v1。
     *
     * @param queryParam
     * @param token
     * @return
     */
    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam, String token) {
        // 根据 token 查询当前登录的用户信息。
        User user = this.userService.queryUserByToken(token);

        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), queryParam.getPage(), queryParam.getPagesize());

        // 推荐用户列表。
        List<RecommendUser> records = pageInfo.getRecords();

        List<Long> userIds = new ArrayList<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }

        // 需要查询用户的信息，并且按照查询条件查询。
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);// 用户 id。

        if (queryParam.getAge() != null) {
            queryWrapper.lt("age", queryParam.getAge());// 年龄小于。
        }

        if (StringUtils.isNotEmpty(queryParam.getCity())) {
            queryWrapper.eq("city", queryParam.getCity());// 城市。
        }

        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);
        List<TodayBest> todayBestList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ","));

            // 缘分值。
            for (RecommendUser record : records) {
                if (record.getUserId().longValue() == todayBest.getId().longValue()) {
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                }
            }

            todayBestList.add(todayBest);
        }

        // 缘分值倒序。
        Collections.sort(todayBestList, (o1, o2) -> (int) (o1.getFateValue() - o2.getFateValue()));

        // 0，前端不需要的数据。
        return new PageResult(0, queryParam.getPagesize(), 0, queryParam.getPage(), todayBestList);
    }

    /**
     * ThreadLocal 版。
     *
     * @param queryParam
     * @return
     */
    public PageResult queryRecommendUserList(RecommendUserQueryParam queryParam) {
        User user = UserThreadLocal.get();

        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), queryParam.getPage(), queryParam.getPagesize());
        List<RecommendUser> records = pageInfo.getRecords();

        // 如果未查询到，需要使用默认推荐列表。
        if (CollectionUtils.isEmpty(records)) {
            String[] ss = StringUtils.split(defaultRecommendUsers, ',');
            for (String s : ss) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());
                recommendUser.setScore(RandomUtils.nextDouble(70, 98));

                records.add(recommendUser);
            }
        }

        List<Long> userIds = new ArrayList<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);// 用户 id。

        if (queryParam.getAge() != null) {
            // queryWrapper.lt("age", queryParam.getAge());// 年龄。
        }

        if (StringUtils.isNotEmpty(queryParam.getCity())) {
            // queryWrapper.eq("city", queryParam.getCity());// 城市。
        }

        // 需要查询用户的信息，并且按照条件查询。
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(queryWrapper);
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            TodayBest todayBest = new TodayBest();

            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

            for (RecommendUser record : records) {
                if (record.getUserId().longValue() == todayBest.getId().longValue()) {
                    double score = Math.floor(record.getScore());
                    todayBest.setFateValue(Double.valueOf(score).longValue());// 缘分值。
                }
            }

            todayBests.add(todayBest);
        }

        // 对结果集做排序，按照缘分值倒序排序。
        Collections.sort(todayBests, (o1, o2) -> Long.valueOf(o2.getFateValue() - o1.getFateValue()).intValue());

        return new PageResult(0, queryParam.getPagesize(), 0, queryParam.getPage(), todayBests);
    }

    public String queryQuestion(Long userId) {
        Question question = this.questionService.queryQuestion(userId);
        if (null != question) {
            return question.getTxt();
        }

        return "";
    }

    /**
     * 回复陌生人问题，发送消息给对方。
     *
     * @param userId
     * @param reply
     * @return
     */
    public Boolean replyQuestion(Long userId, String reply) {
        User user = UserThreadLocal.get();
        UserInfo userInfo = this.userInfoService.queryUserInfoById(user.getId());

        // 构建消息内容。
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", user.getId().toString());
        msg.put("nickname", this.queryQuestion(userId));
        msg.put("strangerQuestion", userInfo.getNickName());
        msg.put("reply", reply);

        try {
            String msgStr = MAPPER.writeValueAsString(msg);

            String targetUrl = this.ssoUrl + "/user/huanxin/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("target", userId.toString());
            params.add("msg", msgStr);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Void> responseEntity = this.restTemplate.postForEntity(targetUrl, httpEntity, Void.class);

            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<NearUserVo> queryNearUser(String gender, String distance) {
        User user = UserThreadLocal.get();
        // 查询当前用户的地理位置。
        UserLocationVo userLocationVo = this.userLocationApi.queryByUserId(user.getId());
        Double longitude = userLocationVo.getLongitude();
        Double latitude = userLocationVo.getLatitude();

        // 查询附近的好友。
        List<UserLocationVo> userLocationVoList = this.userLocationApi.queryUserFromLocation(longitude, latitude, Integer.valueOf(distance));
        if (CollectionUtils.isEmpty(userLocationVoList)) {
            return Collections.emptyList();
        }

        List<Long> userIds = new ArrayList<>();
        for (UserLocationVo locationVo : userLocationVoList) {
            userIds.add(locationVo.getUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.in("user_id", userIds);
        if (StringUtils.equalsIgnoreCase(gender, "man")) {
            queryWrapper.eq("sex", SexEnum.MAN);
        } else if (StringUtils.equalsIgnoreCase(gender, "woman")) {
            queryWrapper.eq("sex", SexEnum.WOMAN);
        }

        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<NearUserVo> result = new ArrayList<>();
        for (UserLocationVo locationVo : userLocationVoList) {
            // 排除自己。
            if (locationVo.getUserId().longValue() == user.getId().longValue()) {
                continue;
            }

            for (UserInfo userInfo : userInfoList) {
                if (locationVo.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    NearUserVo nearUserVo = new NearUserVo();

                    nearUserVo.setNickname(userInfo.getNickName());
                    nearUserVo.setAvatar(userInfo.getLogo());
                    nearUserVo.setUserId(userInfo.getUserId());

                    result.add(nearUserVo);
                    break;
                }
            }
        }

        return result;
    }

    public List<TodayBest> queryCardsList() {
        User user = UserThreadLocal.get();

        PageInfo<RecommendUser> pageInfo = this.recommendUserService.queryRecommendUserList(user.getId(), 1, 50);
        List<RecommendUser> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            // 使用默认的推荐列表。
            String[] ss = StringUtils.split(defaultRecommendUsers, ',');
            for (String s : ss) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());
                records.add(recommendUser);
            }
        }

        List<RecommendUser> newRecommendUserList = new ArrayList<>();

        int showCount = Math.min(10, records.size());
        for (int i = 0; i < showCount; i++) {
            createRecommendUser(newRecommendUserList, records);
        }

        List<Long> userIds = new ArrayList<>();
        for (RecommendUser recommendUser : newRecommendUserList) {
            userIds.add(recommendUser.getUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<TodayBest> result = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            TodayBest todayBest = new TodayBest();

            todayBest.setId(userInfo.getUserId());
            todayBest.setAge(userInfo.getAge());
            todayBest.setAvatar(userInfo.getLogo());
            todayBest.setGender(userInfo.getSex().name().toLowerCase());
            todayBest.setNickname(userInfo.getNickName());
            todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));

            result.add(todayBest);
        }

        return result;
    }

    /**
     * 递归随机生成推荐好友，保证不重复。
     *
     * @param newRecommendUserList
     * @param records
     */
    private void createRecommendUser(List<RecommendUser> newRecommendUserList,
                                     List<RecommendUser> records) {
        RecommendUser recommendUser = records.get(RandomUtils.nextInt(0, records.size() - 1));
        if (!newRecommendUserList.contains(recommendUser)) {
            newRecommendUserList.add(recommendUser);
        } else {
            // 开始递归。
            createRecommendUser(newRecommendUserList, records);
        }
    }

    public Boolean likeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        String id = this.userLikeApi.saveUserLike(user.getId(), likeUserId);
        if (StringUtils.isEmpty(id)) {
            return false;
        }

        if (this.userLikeApi.isMutualLike(user.getId(), likeUserId)) {
            // 相互喜欢成为好友。
            this.imService.contactUser(likeUserId);
        }

        return true;
    }

    public Boolean disLikeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        return this.userLikeApi.deleteUserLike(user.getId(), likeUserId);
    }

}
