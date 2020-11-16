package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.mapper.ISettingsMapper;
import com.tanhua.server.pojo.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    @Autowired
    private ISettingsMapper settingsMapper;

    public Settings querySettingsByUserId(Long userId) {
        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        return this.settingsMapper.selectOne(queryWrapper);
    }

    public void updateNotification(Long userId, Boolean likeNotification, Boolean pinglunNotification, Boolean gonggaoNotification) {
        Settings settings = new Settings();
        settings.setLikeNotification(likeNotification);
        settings.setGonggaoNotification(gonggaoNotification);
        settings.setPinglunNotification(pinglunNotification);

        QueryWrapper<Settings> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        this.settingsMapper.update(settings, queryWrapper);
    }

}
