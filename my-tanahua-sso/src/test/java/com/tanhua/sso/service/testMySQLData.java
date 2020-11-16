package com.tanhua.sso.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class testMySQLData {

    @Test
    public void testMySQLData() {
        System.out.println("INSERT INTO `tb_user` (`id`, `mobile`, `password`, `created`, `updated`) VALUES ('1', '17602026868', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:43:46', '2019-08-02 16:43:46');");
        System.out.println("INSERT INTO `tb_user` (`id`, `mobile`, `password`, `created`, `updated`) VALUES ('2', '15800807988', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:50:32', '2019-08-02 16:50:32');");
        for (int i = 3; i < 100; i++) {
            String mobile = "13" + RandomStringUtils.randomNumeric(9);
            System.out.println("INSERT INTO `tb_user` (`id`, `mobile`, `password`, `created`, `updated`) VALUES('" + i + "', '" + mobile + "', 'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:43:46', '2019-08-0216:43:46');");
        }
    }

}
