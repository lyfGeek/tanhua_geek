package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.UserLike;
import com.tanhua.dubbo.server.vo.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUserLikeApiImpl {

    @Autowired
    private IUserLikeApi userLikeApi;

    @Test
    public void testSave() {
        this.userLikeApi.saveUserLike(1L, 2L);
        this.userLikeApi.saveUserLike(1L, 3L);
        this.userLikeApi.saveUserLike(2L, 1L);
    }

    @Test
    public void testIsMutualLike() {
        System.out.println(this.userLikeApi.isMutualLike(1L, 2L));
        System.out.println(this.userLikeApi.isMutualLike(2L, 1L));
        System.out.println(this.userLikeApi.isMutualLike(1L, 3L));
    }

    @Test
    public void testDeleteUserLike() {
        System.out.println(this.userLikeApi.deleteUserLike(1L, 2L));
        System.out.println(this.userLikeApi.deleteUserLike(1L, 3L));
        System.out.println(this.userLikeApi.deleteUserLike(2L, 1L));
    }

    @Test
    public void testSave2() {
        this.userLikeApi.saveUserLike(1L, 2L);
        this.userLikeApi.saveUserLike(1L, 3L);
        this.userLikeApi.saveUserLike(2L, 1L);
        this.userLikeApi.saveUserLike(1L, 4L);
        this.userLikeApi.saveUserLike(4L, 1L);
        this.userLikeApi.saveUserLike(5L, 1L);
        this.userLikeApi.saveUserLike(6L, 1L);
    }

    @Test
    public void testQueryCounts() {
        System.out.println(this.userLikeApi.queryEachLikeCount(1L));
        System.out.println(this.userLikeApi.queryFanCount(1L));
        System.out.println(this.userLikeApi.queryLikeCount(1L));
    }

    @Test
    public void testQueryList() {
        PageInfo<UserLike> pageInfo = this.userLikeApi.queryEachLikeList(1L, 1, 10);
        for (UserLike record : pageInfo.getRecords()) {
            System.out.println(record);
        }
    }

}
