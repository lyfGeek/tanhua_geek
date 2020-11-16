package com.tanhua.server.pojo;

import com.tanhua.server.enums.SexEnum;
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
