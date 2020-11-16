package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.vo.UserLocationVo;

import java.util.List;

public interface IUserLocationApi {

    /**
     * 更新用户地理位置。
     *
     * @param userId
     * @param longitude
     * @param latitude
     * @param address
     * @return
     */
    String updateUserLocation(Long userId, Double longitude, Double latitude, String address);

    /**
     * 查询用户地理位置。
     *
     * @param userId
     * @return
     */
    UserLocationVo queryByUserId(Long userId);

    /**
     * 根据地理位置查询用户。
     *
     * @param longitude
     * @param latitude
     * @return
     */
    List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range);

}
