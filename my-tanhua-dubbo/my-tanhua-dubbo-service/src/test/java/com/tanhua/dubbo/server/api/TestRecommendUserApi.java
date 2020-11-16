package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.RecommendUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestRecommendUserApi {

    @Autowired
    private IRecommendUserApi recommendUserApi;

    @Test
    public void testQueryWithMaxScore() {
        RecommendUser recommendUser = this.recommendUserApi.queryMaxScore(1L);
        System.out.println(recommendUser);
    }

    @Test
    public void testList() {
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 1, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 2, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 3, 5));
    }

}
