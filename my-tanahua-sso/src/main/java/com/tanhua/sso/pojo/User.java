package com.tanhua.sso.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasePojo {

    private Long id;
    private String mobile;// 手机号。
    @JsonIgnore
    private String password;// 密码，json 序列化时忽略。

}


/*

CREATE TABLE `tb_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mobile` varchar(11) DEFAULT NULL COMMENT '手机号。',
  `password` varchar(32) DEFAULT NULL COMMENT '密码，需要加密。',
  `created` datetime DEFAULT NULL,
  `updated` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `mobile` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表。'

CREATE TABLE `tb_user_info` (
`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`user_id` BIGINT(20) NOT NULL COMMENT '用户 id。',
`nick_name` VARCHAR(50) DEFAULT NULL COMMENT '昵称。',
`logo` VARCHAR(100) DEFAULT NULL COMMENT '用户头像。',
`tags` VARCHAR(50) DEFAULT NULL COMMENT '用户标签：多个用逗号分隔。。',
`sex` TINYINT(1) DEFAULT '3' COMMENT '性别，1-男，2-女，3-未知。',
`age` INT(11) DEFAULT NULL COMMENT '用户年龄。',
`edu` VARCHAR(20) DEFAULT NULL COMMENT '学历。',
`city` VARCHAR(20) DEFAULT NULL COMMENT '居住城市。',
`birthday` VARCHAR(20) DEFAULT NULL COMMENT '生日。',
`cover_pic` VARCHAR(50) DEFAULT NULL COMMENT '封面图片。',
`industry` VARCHAR(20) DEFAULT NULL COMMENT '行业。',
`income` VARCHAR(20) DEFAULT NULL COMMENT '收入。',
`marriage` VARCHAR(20) DEFAULT NULL COMMENT '婚姻状态。',
`created` DATETIME DEFAULT NULL,
`updated` DATETIME DEFAULT NULL,
PRIMARY KEY (`id`),
KEY `user_id` (`user_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT='用户信息表。';

 */
