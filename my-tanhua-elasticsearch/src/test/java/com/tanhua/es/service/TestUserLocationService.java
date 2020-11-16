package com.tanhua.es.service;

import com.tanhua.es.pojo.UserLocationES;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUserLocationService {

    @Autowired
    private UserLocationService userLocationService;

    @Test
    public void testUpdateUserLocation() {
        this.userLocationService.updateUserLocation(1L, 121.512253, 31.24094, "金茂大厦");
        this.userLocationService.updateUserLocation(2L, 121.506377, 31.245105, "东方明珠广播电视塔");
        this.userLocationService.updateUserLocation(10L, 121.508815, 31.243844, "陆家嘴地铁站");
        this.userLocationService.updateUserLocation(12L, 121.511999, 31.239185, "上海中心大厦");
        this.userLocationService.updateUserLocation(25L, 121.493444, 31.240513, "上海市公安局");
        this.userLocationService.updateUserLocation(27L, 121.494108, 31.247011, "上海外滩美术馆");
        this.userLocationService.updateUserLocation(30L, 121.462452, 31.253463, "上海火车站");
        this.userLocationService.updateUserLocation(32L, 121.81509, 31.157478, "上海浦东国际机场");
        this.userLocationService.updateUserLocation(34L, 121.327908, 31.20033, "虹桥火车站");
        this.userLocationService.updateUserLocation(38L, 121.490155, 31.277476, "鲁迅公园");
        this.userLocationService.updateUserLocation(40L, 121.425511, 31.227831, "中山公园");
        this.userLocationService.updateUserLocation(43L, 121.594194, 31.207786, "张江高科");
    }

    @Test
    public void testQueryByUserId() {
        UserLocationES userLocationES = this.userLocationService.queryByUserId(1L);
        System.out.println(userLocationES);
    }

    @Test
    public void testQuery() {
        Page<UserLocationES> userLocationPage = this.userLocationService.queryUserFromLocation(121.512253, 31.24094, 1000d, 1, 100);
        userLocationPage.forEach(userLocationES -> {
            System.out.println(userLocationES);
            double distance = GeoDistance.ARC.calculate(31.24094, 121.512253, userLocationES.getLocation().getLat(), userLocationES.getLocation().getLon(), DistanceUnit.METERS);
            System.out.println("距离我：" + distance + " 米。");
        });
    }

}
