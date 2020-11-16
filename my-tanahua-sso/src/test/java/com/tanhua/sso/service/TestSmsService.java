package com.tanhua.sso.service;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestSmsService {

    @Autowired
    private SmsService smsService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Test
    public void testSend() {
        String code = this.smsService.sendSms("15629193716");
        System.out.println(code);
    }

    @Test
    public void testRocketMQ() {
        rocketMQTemplate.convertAndSend("geek", "geek");
    }

}
