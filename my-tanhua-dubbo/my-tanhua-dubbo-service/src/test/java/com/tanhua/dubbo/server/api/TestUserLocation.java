package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.vo.UserLocationVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUserLocation {

    @Autowired
    private IUserLocationApi userLocationApi;

    @Test
    public void testUpdate() {
        String id = this.userLocationApi.updateUserLocation(1L, 116.413384, 39.910925, "北京");
        System.out.println(id);
    }

    @Test
    public void testQuery() {
        Double longitude = 121.512253;
        Double latitude = 31.24094;
        Integer range = 5000;
        List<UserLocationVo> userLocationVos = this.userLocationApi.queryUserFromLocation(longitude, latitude, range);
        for (UserLocationVo userLocationVo : userLocationVos) {
            System.out.println(userLocationVo);
        }
    }

    @Test
    public void testQueryLocationByUserId() {
        UserLocationVo userLocationVo = this.userLocationApi.queryByUserId(1L);
        System.out.println(userLocationVo);
    }

    @Test
    public void testSaveTestData() {
        this.userLocationApi.updateUserLocation(1L, 121.512253, 31.24094, "金茂大厦");
        this.userLocationApi.updateUserLocation(2L, 121.506377, 31.245105, "东方明珠广播电视塔");
        this.userLocationApi.updateUserLocation(10L, 121.508815, 31.243844, "陆家嘴地铁站");
        this.userLocationApi.updateUserLocation(12L, 121.511999, 31.239185, "上海中心大厦");
        this.userLocationApi.updateUserLocation(25L, 121.493444, 31.240513, "上海市公安局");
        this.userLocationApi.updateUserLocation(27L, 121.494108, 31.247011, "上海外滩美术馆");
        this.userLocationApi.updateUserLocation(30L, 121.462452, 31.253463, "上海火车站");
        this.userLocationApi.updateUserLocation(32L, 121.81509, 31.157478, "上海浦东国际机场");
        this.userLocationApi.updateUserLocation(34L, 121.327908, 31.20033, "虹桥火车站");
        this.userLocationApi.updateUserLocation(38L, 121.490155, 31.277476, "鲁迅公园");
        this.userLocationApi.updateUserLocation(40L, 121.425511, 31.227831, "中山公园");
        this.userLocationApi.updateUserLocation(43L, 121.594194, 31.207786, "张江高科");
    }

}
