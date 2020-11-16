package com.tanhua.sso.service;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

public class TestMongoGen {

    /**
     * 生成 MongoDB 语句。
     */
    @Test
    public void testMongoDBData() {
        for (int i = 2; i < 100; i++) {
            int score = RandomUtils.nextInt(30, 99);
            System.out.println("db.recommend_user.insert({\"userId\": " + i + ", \"toUserId\": 1, \"score\": " + score + ", \"date\": \"2020/1/1\"})");
        }
        // db.recommend_user.find({})
        // 添加索引。
        // db.recommend_user.createIndex({'toUserId': 1, 'score': -1})
    }
}
