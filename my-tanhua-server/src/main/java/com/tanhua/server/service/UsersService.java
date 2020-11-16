package com.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.server.api.IUserLikeApi;
import com.tanhua.dubbo.server.api.IVisitorsApi;
import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;
import com.tanhua.server.pojo.*;
import com.tanhua.server.utils.UserThreadLocal;
import com.tanhua.server.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {

    @Autowired
    private UserInfoService userInfoService;

    @Reference(version = "1.0.0")
    private IUserLikeApi userLikeApi;

    @Reference(version = "1.0.0")
    private IVisitorsApi visitorsApi;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private RecommendUserService recommendUserService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private BlackListService blackListService;

    public UserInfoVo queryUserInfo(String userID, String huanxinID) {
        User user = UserThreadLocal.get();
        Long userId = user.getId();
        if (StringUtils.isNotBlank(userID)) {
            userId = Long.valueOf(userID);
        } else if (StringUtils.isNotBlank(huanxinID)) {
            userId = Long.valueOf(huanxinID);
        }

        UserInfo userInfo = this.userInfoService.queryUserInfoById(userId);
        if (null == userInfo) {
            return null;
        }

        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setAge(userInfo.getAge() != null ? userInfo.getAge().toString() : null);
        userInfoVo.setAvatar(userInfo.getLogo());
        userInfoVo.setBirthday(userInfo.getBirthday());
        userInfoVo.setEducation(userInfo.getEdu());
        userInfoVo.setCity(userInfo.getCity());
        userInfoVo.setGender(userInfo.getSex().name().toLowerCase());
        userInfoVo.setId(userInfo.getUserId());
        userInfoVo.setIncome(userInfo.getIncome() + "K");
        userInfoVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
        userInfoVo.setNickname(userInfo.getNickName());
        userInfoVo.setProfession(userInfo.getIndustry());
        return userInfoVo;
    }

    public Boolean updateUserInfo(UserInfoVo userInfoVo) {
//        User user = UserThreadLocal.get();
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserId(user.getId());
//        userInfo.setAge(Integer.valueOf(userInfoVo.getAge()));
//        userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoVo.getGender(), "man") ? SexEnum.MAN : SexEnum.WOMAN);
//        userInfo.setBirthday(userInfoVo.getBirthday());
//        userInfo.setCity(userInfoVo.getCity());
//        userInfo.setEdu(userInfoVo.getEducation());
//        userInfo.setIncome(StringUtils.replaceAll(userInfoVo.getIncome(), "K", ""));
//        userInfo.setIndustry(userInfoVo.getProfession());
//        userInfo.setMarriage(userInfoVo.getMarriage() == 1 ? "已婚" : "未婚");
//        return this.userInfoService.updateUserInfoByUserId(userInfo);
        return true;
    }

    public CountsVo queryCounts() {
        User user = UserThreadLocal.get();

        CountsVo countsVo = new CountsVo();

        countsVo.setEachLoveCount(this.userLikeApi.queryEachLikeCount(user.getId()));
        countsVo.setLoveCount(this.userLikeApi.queryLikeCount(user.getId()));
        countsVo.setFanCount(this.userLikeApi.queryFanCount(user.getId()));

        return countsVo;
    }

    public PageResult queryLikeList(Integer type, Integer page, Integer pageSize, String nickname) {
        User user = UserThreadLocal.get();
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPagesize(pageSize);

        // type。1 ~ 互相关注。2 ~ 我关注。3 ~ 粉丝。4 ~ 谁看过我。
        List<Long> userIds = new ArrayList<>();
        switch (type) {
            case 1: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryEachLikeList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getUserId());
                }
                break;
            }
            case 2: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryLikeList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getLikeUserId());
                }
                break;
            }
            case 3: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryFanList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getUserId());
                }
                break;
            }
            case 4: {
                PageInfo<Visitors> pageInfo = this.visitorsApi.topVisitor(user.getId(), page, pageSize);
                for (Visitors record : pageInfo.getRecords()) {
                    userIds.add(record.getVisitorUserId());
                }
                break;
            }
            default: {
                break;
            }
        }

        if (CollectionUtils.isEmpty(userIds)) {
            return pageResult;
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        if (StringUtils.isNotBlank(nickname)) {
            queryWrapper.like("nick_name", nickname);
        }

        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<UserLikeListVo> userLikeListVos = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            UserLikeListVo userLikeListVo = new UserLikeListVo();

            userLikeListVo.setNickname(userInfo.getNickName());
            userLikeListVo.setGender(userInfo.getSex().name().toLowerCase());
            userLikeListVo.setId(userInfo.getUserId());
            userLikeListVo.setEducation(userInfo.getEdu());
            userLikeListVo.setCity(userInfo.getCity());
            userLikeListVo.setAvatar(userInfo.getLogo());
            userLikeListVo.setAge(userInfo.getAge());
            userLikeListVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);

            Double score = this.recommendUserService.queryScore(user.getId(), userInfo.getUserId());
            if (score == 0) {
                score = RandomUtils.nextDouble(30, 90);// 特殊情况下的缘分值的生成，一般情况下都会有值。
            }
            userLikeListVo.setMatchRate(score.intValue());// 缘分值。

            userLikeListVos.add(userLikeListVo);
        }

        pageResult.setItems(userLikeListVos);
        return pageResult;
    }

    public void disLike(Long userId) {
        User user = UserThreadLocal.get();
        this.userLikeApi.deleteUserLike(user.getId(), userId);
    }

    public void likeFan(Long userId) {
        User user = UserThreadLocal.get();
        this.userLikeApi.saveUserLike(user.getId(), userId);
    }

    public SettingsVo querySettings() {
        User user = UserThreadLocal.get();
        SettingsVo settingsVo = new SettingsVo();
        settingsVo.setPhone(user.getMobile());
        settingsVo.setId(user.getId());

        // 查询通用的设置。
        Settings settings = this.settingsService.querySettingsByUserId(user.getId());
        if (null != settings) {
            settingsVo.setPinglunNotification(settings.getPinglunNotification());
            settingsVo.setLikeNotification(settings.getLikeNotification());
            settingsVo.setGonggaoNotification(settings.getGonggaoNotification());
        }

        // 查询陌生人问题。
        Question question = this.questionService.queryQuestion(user.getId());
        if (null != question) {
            settingsVo.setStrangerQuestion(question.getTxt());
        }

        return settingsVo;
    }

    public void saveQuestions(String content) {
        User user = UserThreadLocal.get();
        this.questionService.save(user.getId(), content);
    }

    public PageResult queryBlacklist(Integer page, Integer pagesize) {

        User user = UserThreadLocal.get();
        IPage<BlackList> iPage = this.blackListService.queryPageList(user.getId(), page, pagesize);

        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pagesize);
        pageResult.setPage(page);
        pageResult.setCounts(new Long(iPage.getTotal()).intValue());
        pageResult.setPages(new Long(iPage.getPages()).intValue());

        List<BlackList> records = iPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return pageResult;
        }

        List<Long> userIds = new ArrayList<>();
        for (BlackList record : records) {
            userIds.add(record.getBlackUserId());
        }

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);

        List<BlackListVo> blackListVoList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            BlackListVo blackListVo = new BlackListVo();

            blackListVo.setNickname(userInfo.getNickName());
            blackListVo.setId(userInfo.getUserId());
            blackListVo.setGender(userInfo.getSex().name().toLowerCase());
            blackListVo.setAvatar(userInfo.getLogo());
            blackListVo.setAge(userInfo.getAge());

            blackListVoList.add(blackListVo);
        }

        pageResult.setItems(blackListVoList);

        return pageResult;
    }

    public void delBlacklist(Long userId) {
        User user = UserThreadLocal.get();
        this.blackListService.delBlacklist(user.getId(), userId);
    }

    public void updateNotification(Boolean likeNotification, Boolean pinglunNotification, Boolean gonggaoNotification) {
        User user = UserThreadLocal.get();
        this.settingsService.updateNotification(user.getId(), likeNotification, pinglunNotification, gonggaoNotification);
    }

}
