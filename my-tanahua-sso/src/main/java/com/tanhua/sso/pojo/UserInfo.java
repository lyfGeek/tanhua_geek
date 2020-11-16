package com.tanhua.sso.pojo;

import com.tanhua.sso.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends BasePojo {

    private Long id;
    private Long userId;// 用户 id。
    private String nickName;// 昵称。
    private String logo;// 用户头像。
    private String tags;// 用户标签：多个用逗号分隔。
    private SexEnum sex;// 性别。
    private Integer age;// 年龄。
    private String edu;// 学历。
    private String city;// 城市。
    private String birthday;// 生日。
    private String coverPic;// 封面图片。
    private String industry;// 行业。
    private String income;// 收入。
    private String marriage;// 婚姻状态。

}


/*

CREATE TABLE `tb_user_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 id。',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称。',
  `logo` varchar(100) DEFAULT NULL COMMENT '用户头像。',
  `tags` varchar(50) DEFAULT NULL COMMENT '用户标签：多个用逗号分隔。。',
  `sex` tinyint(1) DEFAULT '3' COMMENT '性别，1-男，2-女，3-未知。',
  `age` int(11) DEFAULT NULL COMMENT '用户年龄。',
  `edu` varchar(20) DEFAULT NULL COMMENT '学历。',
  `city` varchar(20) DEFAULT NULL COMMENT '居住城市。',
  `birthday` varchar(20) DEFAULT NULL COMMENT '生日。',
  `cover_pic` varchar(200) DEFAULT NULL COMMENT '封面图片。',
  `industry` varchar(20) DEFAULT NULL COMMENT '行业。',
  `income` varchar(20) DEFAULT NULL COMMENT '收入。',
  `marriage` varchar(20) DEFAULT NULL COMMENT '婚姻状态。',
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户信息表。'

 */
