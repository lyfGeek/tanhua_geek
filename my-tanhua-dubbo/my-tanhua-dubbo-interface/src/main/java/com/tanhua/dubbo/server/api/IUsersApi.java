package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Users;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface IUsersApi {

    /**
     * 保存好友。
     *
     * @param users
     * @return
     */
    String saveUsers(Users users);

    /**
     * 根据用户 id 查询 users 列表。
     *
     * @param userId
     * @return
     */
    List<Users> queryAllUsersList(Long userId);

    /**
     * 根据用户 id 查询 users 列表（分页查询）。
     *
     * @param userId
     * @return
     */
    PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize);

}
