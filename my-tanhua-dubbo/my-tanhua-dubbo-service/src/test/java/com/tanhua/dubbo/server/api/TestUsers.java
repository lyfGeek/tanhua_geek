package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Users;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestUsers {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void saveUsers() {
        // 用户 ID 为 1 的用户的 5 个好友。
        this.mongoTemplate.save(new Users(ObjectId.get(), 1L, 2L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(), 1L, 3L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(), 1L, 4L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(), 1L, 5L, System.currentTimeMillis()));
        this.mongoTemplate.save(new Users(ObjectId.get(), 1L, 6L, System.currentTimeMillis()));
    }

    @Test
    public void testQueryList() {
        Criteria criteria = Criteria.where("userId").is(1L);
        List<Users> users = this.mongoTemplate.find(Query.query(criteria), Users.class);
        for (Users user : users) {
            System.out.println(user);
        }
    }

}
