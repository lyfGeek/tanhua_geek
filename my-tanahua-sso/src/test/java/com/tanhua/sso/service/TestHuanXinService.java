package com.tanhua.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestHuanXinService {

    @Autowired
    private HuanXinService huanXinService;

    @Test
    public void tesRegister() {
        boolean register = this.huanXinService.register(1L);
        System.out.println(register);
    }

}
