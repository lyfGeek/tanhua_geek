package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Video;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface IVideoApi {

    /**
     * 保存小视频。
     *
     * @param video
     * @return
     */
    String saveVideo(Video video);

    Boolean saveVideoBool(Video video);

    /**
     * 分页查询小视频列表，按照时间倒序排序。
     *
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Video> queryVideoList(Integer page, Integer pageSize);

    /**
     * 关注用户。
     *
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean followUser(Long userId, Long followUserId);

    /**
     * 取消关注用户。
     *
     * @param userId
     * @param followUserId
     * @return
     */
    Boolean disFollowUser(Long userId, Long followUserId);

    /**
     * 根据 id 查询小视频。
     *
     * @param id
     * @return
     */
    Video queryVideoById(String id);

    /**
     * 根据 vids 批量查询视频列表。
     *
     * @param vids
     * @return
     */
    List<Video> queryVideoListByPids(List<Long> vids);

}
