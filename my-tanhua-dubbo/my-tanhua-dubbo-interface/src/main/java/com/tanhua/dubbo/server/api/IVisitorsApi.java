package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Visitors;
import com.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface IVisitorsApi {

    /**
     * 保存来访记录。
     *
     * @param visitors
     * @return
     */
    String saveVisitor(Visitors visitors);

    /**
     * 查询最近的访客信息。按照时间倒序排序。
     *
     * @param userId
     * @param num
     * @return
     */
    List<Visitors> topVisitor(Long userId, Integer num);

    /**
     * 查询最近的访客信息。按照时间倒序排序。
     *
     * @param userId
     * @param date
     * @return
     */
    List<Visitors> topVisitor(Long userId, Long date);

    /**
     * 查询最近的访客信息。按照时间倒序排序。
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    PageInfo<Visitors> topVisitor(Long userId, Integer page, Integer pageSize);

}
