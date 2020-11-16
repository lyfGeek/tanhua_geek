package com.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {

    private Long id;
    private String avatar;// 头像。
    private String nickname;// 昵称。
    private String birthday;// 生日 2019-09-11。
    private String age;// 年龄。
    private String gender;// 性别 man woman。
    private String city;// 城市。
    private String education;// 学历 枚举：本科,硕士,双硕,博士,双博。
    private String income;// 月收入 枚举：5k,8K,15K,35K,55K,80K,100K。
    private String profession;// 行业 枚举：IT行业,服务行业,公务员。
    private Integer marriage;// 婚姻状态（0未婚，1已婚）。

}
