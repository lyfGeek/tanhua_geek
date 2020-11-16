package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.server.mapper.IUserInfoMapper;
import com.tanhua.server.pojo.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {

    @Autowired
    private IUserInfoMapper userInfoMapper;

    /**
     * 查询数据库，查找用户的信息数据。
     * 说明：为了简单处理，直接查询数据库了。建议：编写 Dubbo 服务，进行调用。
     *
     * @param id
     * @return
     */
    public UserInfo queryUserInfoById(Long id) {
        return this.userInfoMapper.selectById(id);
    }

    /**
     * 查询用户信息列表。
     *
     * @param queryWrapper
     * @return
     */
    public List<UserInfo> queryUserInfoList(QueryWrapper queryWrapper) {
        return this.userInfoMapper.selectList(queryWrapper);
    }

    public Boolean updateUserInfoByUserId(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userInfo.getUserId());
        return this.userInfoMapper.update(userInfo, queryWrapper) > 0;
    }

}
