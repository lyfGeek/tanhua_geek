package com.tanhua.dubbo.server.api;

import com.tanhua.dubbo.server.pojo.Visitors;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestVisitors {

    @Autowired
    private IVisitorsApi visitorsApi;

    @Test
    public void testSave() {
        for (int i = 0; i < 100; i++) {
            Visitors visitors = new Visitors();

            visitors.setFrom("首页");
            visitors.setUserId(RandomUtils.nextLong(1, 10));
            visitors.setVisitorUserId(RandomUtils.nextLong(11, 50));

            this.visitorsApi.saveVisitor(visitors);
        }
        System.out.println("ok");
    }

    @Test
    public void testQuery() {
        List<Visitors> visitors = this.visitorsApi.topVisitor(1L, 10);
        for (Visitors visitor : visitors) {
            System.out.println(visitor);
        }
    }

}
